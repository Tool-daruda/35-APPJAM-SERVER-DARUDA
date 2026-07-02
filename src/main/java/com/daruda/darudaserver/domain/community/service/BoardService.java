package com.daruda.darudaserver.domain.community.service;

import static com.daruda.darudaserver.domain.community.entity.QBoard.*;
import static com.daruda.darudaserver.domain.community.entity.QBoardScrap.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.dto.req.BoardCreateAndUpdateReq;
import com.daruda.darudaserver.domain.community.dto.res.BoardRes;
import com.daruda.darudaserver.domain.community.dto.res.GetBoardResponse;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.entity.BoardImage;
import com.daruda.darudaserver.domain.community.entity.BoardScrap;
import com.daruda.darudaserver.domain.community.entity.BoardSortType;
import com.daruda.darudaserver.domain.community.event.BoardCreatedEvent;
import com.daruda.darudaserver.domain.community.event.BoardUpdatedEvent;
import com.daruda.darudaserver.domain.community.repository.BoardImageRepository;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.community.repository.BoardScrapRepository;
import com.daruda.darudaserver.domain.community.util.ValidateBoard;
import com.daruda.darudaserver.domain.search.document.BoardDocument;
import com.daruda.darudaserver.domain.search.repository.BoardSearchRepository;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.repository.ToolRepository;
import com.daruda.darudaserver.domain.user.dto.response.BoardListResponse;
import com.daruda.darudaserver.domain.user.dto.response.PagenationDto;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.ForbiddenException;
import com.daruda.darudaserver.global.error.exception.InvalidValueException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.error.exception.UnauthorizedException;
import com.daruda.darudaserver.global.image.repository.ImageRepository;
import com.daruda.darudaserver.global.image.service.ImageService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BoardService {

	private static final String TOOL_LOGO = "https://daruda.s3.ap-northeast-2.amazonaws.com/Cursor_logo.png";
	private static final String FREE = "자유";

	private final BoardRepository boardRepository;
	private final BoardImageService boardImageService;
	private final BoardImageRepository boardImageRepository;
	private final ImageService imageService;
	private final ImageRepository imageRepository;
	private final UserRepository userRepository;
	private final BoardScrapRepository boardScrapRepository;
	private final BoardScrapService boardScrapService;
	private final ToolRepository toolRepository;
	private final CommentRepository commentRepository;
	private final ValidateBoard validateBoard;
	private final JPAQueryFactory jpaQueryFactory;
	private final BoardSearchRepository boardSearchRepository;
	private final ApplicationEventPublisher eventPublisher;

	// 게시판 생성
	public BoardRes createBoard(final Long userId, final BoardCreateAndUpdateReq boardCreateAndUpdateReq) {
		log.info("유저아이디: {}", userId);
		UserEntity user = getUserById(userId);

		// 제재 상태 확인
		if (user.isSuspended()) {
			throw new ForbiddenException(ErrorCode.USER_SUSPENDED);
		}

		Tool tool = getToolById(boardCreateAndUpdateReq.toolId());
		Board board = boardCreateAndUpdateReq.isFree()
			? createFreeBoard(user, boardCreateAndUpdateReq) :
			createToolBoard(tool, boardCreateAndUpdateReq, user);

		// 이미지 처리
		List<String> imageUrls = processImages(board, boardCreateAndUpdateReq.imageList());

		eventPublisher.publishEvent(
			new BoardCreatedEvent(board.getId())
		);

		// Tool 정보 설정
		String toolName = board.getTool() != null ? board.getTool().getToolMainName() : FREE;
		String toolLogo = board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO;

		return BoardRes.of(board, toolName, toolLogo, getCommentCount(board.getId()), imageUrls, tool.getToolId());
	}

	// 게시판 업데이트
	public BoardRes updateBoard(final Long userId, final Long boardId,
		final BoardCreateAndUpdateReq boardCreateAndUpdateReq) {
		Board board = validateBoardAndUser(userId, boardId);
		UserEntity user = board.getUser();

		// 제재 상태 확인
		if (user.isSuspended()) {
			throw new ForbiddenException(ErrorCode.USER_SUSPENDED);
		}

		boardSearchRepository.findById(boardId.toString())
			.orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));

		Tool tool = boardCreateAndUpdateReq.isFree() ? null : getToolById(boardCreateAndUpdateReq.toolId());

		board.update(
			tool,
			user,
			boardCreateAndUpdateReq.title(),
			boardCreateAndUpdateReq.content(),
			boardCreateAndUpdateReq.isFree()
		);

		eventPublisher.publishEvent(
			new BoardUpdatedEvent(board.getId())
		);

		List<String> imageUrls = processImages(board, boardCreateAndUpdateReq.imageList());

		String toolName = board.getTool() != null ? board.getTool().getToolMainName() : FREE;
		String toolLogo = board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO;

		boolean isScrapped = boardScrapService.isScraped(user, board);

		return BoardRes.of(board, toolName, toolLogo, getCommentCount(board.getId()), imageUrls, isScrapped);
	}

	// 게시판 삭제
	public void deleteBoard(final Long userId, final Long boardId) {
		Board board = validateBoardAndUser(userId, boardId);
		BoardDocument boardDocument = boardSearchRepository.findById(boardId.toString())
			.orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));
		deleteOriginImages(boardId);

		List<CommentEntity> commentEntityList = commentRepository.findCommentsByBoardId(boardId);
		if (!commentEntityList.isEmpty()) {
			commentRepository.deleteAll(commentEntityList);
			log.info("삭제된 게시글과 연관된 댓글 데이터를 제거했습니다. Comment Count: {}", commentEntityList.size());
		}

		List<BoardScrap> scraps = boardScrapRepository.findAllByBoardId(boardId);
		if (!scraps.isEmpty()) {
			boardScrapRepository.deleteAll(scraps);
			log.info("삭제된 게시글과 연관된 스크랩 데이터를 제거했습니다. Scrap Count: {}", scraps.size());
		}
		board.delete();
		boardSearchRepository.delete(boardDocument);
	}

	// 게시판 조회
	public BoardRes getBoard(final Long userIdOrNull, final Long boardId) {
		UserEntity user = getUser(userIdOrNull);
		Board board = getBoardById(boardId);
		Long toolId = getToolId(boardId);
		List<String> imageUrls = boardImageService.getBoardImageUrls(boardId);

		String toolName = board.getTool() != null ? board.getTool().getToolMainName() : FREE;
		String toolLogo = board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO;
		Boolean isScraped = boardScrapService.isScraped(user, board);
		return BoardRes.of(board, toolName, toolLogo, getCommentCount(boardId), imageUrls, isScraped, toolId);
	}

	// 내가 쓴  게시판 조회
	public BoardRes getMyBoard(final UserEntity user, final Long boardId) {
		Board board = getBoardById(boardId);
		List<String> imageUrls = boardImageService.getBoardImageUrls(boardId);
		String toolName = board.getTool() != null ? board.getTool().getToolMainName() : FREE;
		String toolLogo = board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO;

		Boolean isScraped = boardScrapService.isScraped(user, board);
		return BoardRes.of(board, toolName, toolLogo, getCommentCount(boardId), imageUrls, isScraped);
	}

	public GetBoardResponse getBoardList(final Long userIdOrNull, final Boolean noTopic, final Long toolId,
		final int size, final Long lastBoardId, final BoardSortType sortType, final Long lastScrapCount) {

		log.info("USERID OR NULL {}", userIdOrNull);
		if (size < 1) {
			throw new InvalidValueException(ErrorCode.INVALID_FIELD_ERROR);
		}
		UserEntity user = getUser(userIdOrNull);
		log.info("USER : {}", user);

		//NoTopic = null, toolId = null -> 전체 게시판 조회
		//NoTopic = False , toolId != null -> 툴 게시판
		//NoTopic = True , toolId == null -> 자유게시판
		if (noTopic != null) {
			if ((noTopic.equals(Boolean.TRUE) && toolId != null) || (noTopic.equals(Boolean.FALSE) && toolId == null)) {
				throw new InvalidValueException(ErrorCode.INVALID_FIELD_ERROR);
			}
		}

		// 전체 데이터 개수를 가져옴 (cursor 조건 없음)
		long totalElements = Optional.ofNullable(jpaQueryFactory
			.select(board.count())
			.from(board)
			.where(
				board.delYn.eq(false),
				noTopic != null ? board.isFree.eq(noTopic) : null,
				toolId != null ? board.tool.toolId.eq(toolId) : null
			)
			.fetchFirst()).orElse(0L);

		final BoardSortType effectiveSortType = sortType == null ? BoardSortType.LATEST : sortType;
		List<Board> paginatedBoards;
		boolean hasNextPage;
		long nextCursor;
		long nextScrapCount = -1L;
		// board별 스크랩 수. SCRAP/LATEST 분기에서 채우는 경로는 다르지만 두 값의 의미(해당 board의 BoardScrap 총 개수)는 동일해야 한다.
		Map<Long, Long> scrapCountMap = new HashMap<>();

		if (effectiveSortType == BoardSortType.SCRAP) {
			if ((lastBoardId == null) ^ (lastScrapCount == null)) {
				throw new InvalidValueException(ErrorCode.INVALID_FIELD_ERROR);
			}

			NumberExpression<Long> scrapCountExpr = Expressions.asNumber(
				com.querydsl.jpa.JPAExpressions
					.select(boardScrap.count())
					.from(boardScrap)
					.where(boardScrap.board.id.eq(board.id))
			);

			BooleanBuilder where = new BooleanBuilder();
			where.and(board.delYn.eq(Boolean.FALSE));
			if (noTopic != null) {
				where.and(board.isFree.eq(noTopic));
			}
			if (toolId != null) {
				where.and(board.tool.toolId.eq(toolId));
			}
			if (lastScrapCount != null && lastBoardId != null) {
				where.and(
					scrapCountExpr.lt(lastScrapCount)
						.or(scrapCountExpr.eq(lastScrapCount).and(board.id.lt(lastBoardId)))
				);
			}

			List<Tuple> results = jpaQueryFactory
				.select(board, scrapCountExpr)
				.from(board)
				.where(where)
				.orderBy(scrapCountExpr.desc(), board.id.desc())
				.limit(size + 1L)
				.fetch();

			hasNextPage = results.size() > size;
			List<Tuple> pagedResults = hasNextPage ? results.subList(0, size) : results;
			paginatedBoards = pagedResults.stream().map(t -> t.get(board)).toList();

			// 정렬 시 이미 조회한 스크랩 수를 재사용 → 추가 쿼리 없이, 커서(nextScrapCount)와 표시값의 시점도 일치
			for (Tuple tuple : pagedResults) {
				Long scrapCount = tuple.get(scrapCountExpr);
				scrapCountMap.put(tuple.get(board).getId(), scrapCount == null ? 0L : scrapCount);
			}

			if (hasNextPage) {
				Tuple lastInPage = pagedResults.get(pagedResults.size() - 1);
				nextCursor = lastInPage.get(board).getId();
				Long lastCount = lastInPage.get(scrapCountExpr);
				nextScrapCount = lastCount == null ? 0L : lastCount;
			} else {
				nextCursor = -1L;
			}
		} else {
			Long cursor = (lastBoardId == null) ? Long.MAX_VALUE : lastBoardId + 1;

			List<Board> boards = jpaQueryFactory
				.selectFrom(board)
				.where(
					noTopic != null ? board.isFree.eq(noTopic) : null,
					toolId != null ? board.tool.toolId.eq(toolId) : null,
					board.delYn.eq(Boolean.FALSE),
					board.id.lt(cursor)
				)
				.orderBy(board.id.desc())
				.limit(size + 1)
				.fetch();

			hasNextPage = boards.size() > size;
			paginatedBoards = hasNextPage ? boards.subList(0, size) : boards;
			nextCursor = hasNextPage ? boards.get(size).getId() : -1L;

			// LATEST 정렬은 스크랩 수를 따로 조회하지 않으므로 배치 쿼리로 한 번에 조회 (N+1 방지)
			List<Long> boardIds = paginatedBoards.stream().map(Board::getId).toList();
			if (!boardIds.isEmpty()) {
				scrapCountMap.putAll(boardScrapRepository.countMapByBoardIds(boardIds));
			}
		}

		// 응답 데이터
		List<BoardRes> boardResList = paginatedBoards.stream()
			.map(board -> {
				String toolName;
				String toolLogo;
				Long savedToolid = null;
				if (Boolean.FALSE.equals(noTopic) && toolId != null) {  // 툴 게시판
					toolName = board.getTool().getToolMainName();
					toolLogo = board.getTool().getToolLogo();
					savedToolid = board.getTool().getToolId();
				} else if (Boolean.TRUE.equals(noTopic) && toolId == null) { // 자유 게시판
					toolName = FREE;
					toolLogo = TOOL_LOGO;
				} else { // 전체 게시판 (툴이 있는 경우만 가져옴)
					toolName = (board.getTool() != null) ? board.getTool().getToolMainName() : FREE;
					toolLogo = (board.getTool() != null) ? board.getTool().getToolLogo() : TOOL_LOGO;
					savedToolid = (board.getTool() != null) ? board.getTool().getToolId() : null;
				}

				int commentCount = getCommentCount(board.getId());
				List<String> boardImages = boardImageService.getBoardImageUrls(board.getId());
				boolean isScrapped = boardScrapService.isScraped(user, board);
				long scrapCount = scrapCountMap.getOrDefault(board.getId(), 0L);
				return BoardRes.of(board, toolName, toolLogo, commentCount, boardImages, isScrapped, savedToolid,
					scrapCount);
			})
			.toList();

		ScrollPaginationDto scrollPaginationDto = ScrollPaginationDto.of(totalElements, nextCursor);
		Long responseNextScrapCount =
			(effectiveSortType == BoardSortType.SCRAP && nextScrapCount >= 0) ? nextScrapCount : null;
		return GetBoardResponse.of(boardResList, scrollPaginationDto, responseNextScrapCount);
	}

	private Board validateBoardAndUser(final Long userId, final Long boardId) {
		Board board = getBoardById(boardId);
		if (!board.getUser().getId().equals(userId)) {
			log.debug("게시판 작성자와, 유저가 다릅니다.");
			throw new UnauthorizedException(ErrorCode.BOARD_FORBIDDEN);
		}
		return board;
	}

	public List<String> processImages(final Board board, final List<String> newImageUrls) {
		List<BoardImage> existingBoardImages = boardImageRepository.findAllByBoardId(board.getId());

		if (existingBoardImages.isEmpty() && (newImageUrls == null || newImageUrls.isEmpty())) {
			return List.of();
		}

		List<Long> existingImageIds = existingBoardImages.stream()
			.map(BoardImage::getImageId)
			.toList();

		List<com.daruda.darudaserver.global.image.entity.Image> existingImages = existingImageIds.isEmpty()
			? List.of() : imageRepository.findAllById(existingImageIds);

		List<String> validNewUrls = newImageUrls == null ? List.of() : newImageUrls.stream()
			.filter(url -> url != null && !url.isBlank())
			.toList();

		// 삭제 대상 식별 (기존에 있었지만 새로 전달받지 않은 URL)
		List<Long> imageIdsToDelete = existingImages.stream()
			.filter(img -> !validNewUrls.contains(img.getImageUrl()))
			.map(com.daruda.darudaserver.global.image.entity.Image::getImageId)
			.toList();

		if (!imageIdsToDelete.isEmpty()) {
			List<BoardImage> boardImagesToDelete = existingBoardImages.stream()
				.filter(bi -> imageIdsToDelete.contains(bi.getImageId()))
				.toList();
			boardImageRepository.deleteAll(boardImagesToDelete);
			imageService.deleteImages(imageIdsToDelete);
		}

		// 추가 대상 식별 (새로 전달받았지만 기존에 없던 URL)
		List<String> existingUrls = existingImages.stream()
			.map(com.daruda.darudaserver.global.image.entity.Image::getImageUrl)
			.toList();

		List<String> urlsToAdd = validNewUrls.stream()
			.filter(url -> !existingUrls.contains(url))
			.toList();

		if (!urlsToAdd.isEmpty()) {
			List<Long> newImageIds = imageService.createImage(urlsToAdd);
			boardImageService.saveBoardImages(board.getId(), newImageIds);
		}

		return boardImageService.getBoardImageUrls(board.getId());
	}

	private Board createToolBoard(final Tool tool, final BoardCreateAndUpdateReq req, final UserEntity user) {
		Board board = Board.create(tool, user, req.title(), req.content());
		board = boardRepository.save(board);

		return board;
	}

	private Board createFreeBoard(final UserEntity user, final BoardCreateAndUpdateReq req) {
		Board board = Board.createFree(user, req.title(), req.content());
		board = boardRepository.save(board);

		return board;
	}

	private Board getBoardById(final Long boardId) {
		return boardRepository.findByIdAndDelYn(boardId, false)
			.orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));
	}

	public Tool getToolById(final Long toolId) {
		return toolRepository.findById(toolId).orElseThrow(() -> new NotFoundException(ErrorCode.TOOL_NOT_FOUND));
	}

	public UserEntity getUserById(final Long userId) {
		return userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
	}

	private void deleteOriginImages(final Long boardId) {
		List<BoardImage> boardImages = boardImageRepository.findAllByBoardId(boardId);
		List<Long> imageIds = boardImages.stream().map(BoardImage::getImageId).toList();
		boardImageRepository.deleteAll(boardImages);
		imageService.deleteImages(imageIds);
	}

	public BoardListResponse getUserBoards(Long userIdOrNull, Pageable pageable) {
		validateBoard.validateUser(userIdOrNull);
		log.debug("사용자를 조회합니다, {}", userIdOrNull);
		Page<Board> boards = boardRepository.findAllByUserIdAndDelYnFalse(userIdOrNull, pageable);
		UserEntity user = getUser(userIdOrNull);
		List<BoardRes> boardResList = boards.getContent().stream()
			.map(board -> getMyBoard(user, board.getId()))
			.toList();

		PagenationDto pageInfo = PagenationDto.of(pageable.getPageNumber(), pageable.getPageSize(),
			boards.getTotalPages());

		return new BoardListResponse(boardResList, userIdOrNull, pageInfo);
	}

	public int getCommentCount(final Long boardId) {
		List<CommentEntity> commentEntityList = commentRepository.findCommentsByBoardId(boardId);
		log.debug("댓글 Entity리스트를 받아옵니다 : {}", commentEntityList.size());
		return commentEntityList.size();
	}

	public UserEntity getUser(final Long userIdOrNull) {
		UserEntity user = null;
		if (userIdOrNull != null) {
			user = userRepository.findById(userIdOrNull)
				.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
			log.debug("유저 정보를 조회했습니다: {}", user.getId());
		}
		return user;
	}

	public Long getToolId(Long boardId) {
		Board board = getBoardById(boardId);
		return board.isFree() ? null : board.getTool().getToolId();
	}
}
