package com.daruda.darudaserver.domain.community.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.comment.entity.Comment;
import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.dto.request.BoardCreateAndUpdateRequest;
import com.daruda.darudaserver.domain.community.dto.response.BoardResponse;
import com.daruda.darudaserver.domain.community.dto.response.GetBoardResponse;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.entity.BoardImage;
import com.daruda.darudaserver.domain.community.entity.BoardScrap;
import com.daruda.darudaserver.domain.community.entity.BoardSortType;
import com.daruda.darudaserver.domain.community.event.BoardCreatedEvent;
import com.daruda.darudaserver.domain.community.event.BoardUpdatedEvent;
import com.daruda.darudaserver.domain.community.repository.BoardImageRepository;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.community.repository.BoardScrapCountRow;
import com.daruda.darudaserver.domain.community.repository.BoardScrapRepository;
import com.daruda.darudaserver.domain.community.util.ValidateBoard;
import com.daruda.darudaserver.domain.search.repository.BoardSearchRepository;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.repository.ToolRepository;
import com.daruda.darudaserver.domain.user.dto.response.BoardListResponse;
import com.daruda.darudaserver.domain.user.dto.response.PagenationDto;
import com.daruda.darudaserver.domain.user.entity.User;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.ForbiddenException;
import com.daruda.darudaserver.global.error.exception.InvalidValueException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.error.exception.UnauthorizedException;
import com.daruda.darudaserver.global.image.repository.ImageRepository;
import com.daruda.darudaserver.global.image.service.ImageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(readOnly = true)
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
	private final BoardSearchRepository boardSearchRepository;
	private final ApplicationEventPublisher eventPublisher;

	// 게시판 생성
	@Transactional
	public BoardResponse createBoard(final Long userId, final BoardCreateAndUpdateRequest boardCreateAndUpdateReq) {
		log.info("유저아이디: {}", userId);
		User user = getUserById(userId);

		// 제재 상태 확인
		if (user.isSuspended()) {
			throw new ForbiddenException(ErrorCode.USER_SUSPENDED);
		}

		// 자유 게시판(isFree=true)은 toolId가 없으므로 Tool을 조회하지 않는다. (toolId=null로 조회 시 예외 발생 방지)
		Board board;
		Long toolId;
		if (boardCreateAndUpdateReq.isFree()) {
			board = createFreeBoard(user, boardCreateAndUpdateReq);
			toolId = null;
		} else {
			Tool tool = getToolById(boardCreateAndUpdateReq.toolId());
			board = createToolBoard(tool, boardCreateAndUpdateReq, user);
			toolId = tool.getToolId();
		}

		// 이미지 처리
		List<String> imageUrls = processImages(board, boardCreateAndUpdateReq.imageList());

		eventPublisher.publishEvent(
			new BoardCreatedEvent(board.getId())
		);

		// Tool 정보 설정
		String toolName = board.getTool() != null ? board.getTool().getToolMainName() : FREE;
		String toolLogo = board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO;

		return BoardResponse.of(board, toolName, toolLogo, getCommentCount(board.getId()), imageUrls, toolId);
	}

	// 게시판 업데이트
	@Transactional
	public BoardResponse updateBoard(final Long userId, final Long boardId,
		final BoardCreateAndUpdateRequest boardCreateAndUpdateReq) {
		Board board = validateBoardAndUser(userId, boardId);
		User user = board.getUser();

		// 제재 상태 확인
		if (user.isSuspended()) {
			throw new ForbiddenException(ErrorCode.USER_SUSPENDED);
		}

		// 게시글 존재 여부는 validateBoardAndUser(DB)로 이미 검증된다.
		// 검색 색인(ES) 문서 유무로 판단하면 색인 지연/유실 시 정상 게시글도 수정 불가가 되므로 사전 검증하지 않는다.
		// 수정 후 BoardUpdatedEvent 처리에서 ES 문서를 upsert(save)하여 재색인한다.
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

		return BoardResponse.of(board, toolName, toolLogo, getCommentCount(board.getId()), imageUrls, isScrapped);
	}

	// 게시판 삭제
	@Transactional
	public void deleteBoard(final Long userId, final Long boardId) {
		Board board = validateBoardAndUser(userId, boardId);
		deleteOriginImages(boardId);

		List<Comment> commentEntityList = commentRepository.findCommentsByBoardId(boardId);
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
		// 검색 색인(ES) 문서가 없어도 삭제가 실패하지 않도록 idempotent하게 처리한다.
		boardSearchRepository.deleteById(boardId.toString());
	}

	// 게시판 조회
	public BoardResponse getBoard(final Long userIdOrNull, final Long boardId) {
		User user = getUser(userIdOrNull);
		Board board = getBoardById(boardId);
		Long toolId = getToolId(boardId);
		List<String> imageUrls = boardImageService.getBoardImageUrls(boardId);

		String toolName = board.getTool() != null ? board.getTool().getToolMainName() : FREE;
		String toolLogo = board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO;
		Boolean isScraped = boardScrapService.isScraped(user, board);
		return BoardResponse.of(board, toolName, toolLogo, getCommentCount(boardId), imageUrls, isScraped, toolId);
	}

	public GetBoardResponse getBoardList(final Long userIdOrNull, final Boolean noTopic, final Long toolId,
		final int size, final Long lastBoardId, final BoardSortType sortType, final Long lastScrapCount) {

		log.info("USERID OR NULL {}", userIdOrNull);
		if (size < 1) {
			throw new InvalidValueException(ErrorCode.INVALID_FIELD_ERROR);
		}
		User user = getUser(userIdOrNull);
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
		long totalElements = boardRepository.countBoards(noTopic, toolId);

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

			List<BoardScrapCountRow> results = boardRepository.findBoardsByScrapCountDesc(
				noTopic, toolId, lastScrapCount, lastBoardId, size + 1L);

			hasNextPage = results.size() > size;
			List<BoardScrapCountRow> pagedResults = hasNextPage ? results.subList(0, size) : results;
			paginatedBoards = pagedResults.stream().map(BoardScrapCountRow::board).toList();

			// 정렬 시 이미 조회한 스크랩 수를 재사용 → 추가 쿼리 없이, 커서(nextScrapCount)와 표시값의 시점도 일치
			for (BoardScrapCountRow row : pagedResults) {
				scrapCountMap.put(row.board().getId(), row.scrapCount());
			}

			if (hasNextPage) {
				BoardScrapCountRow lastInPage = pagedResults.get(pagedResults.size() - 1);
				nextCursor = lastInPage.board().getId();
				nextScrapCount = lastInPage.scrapCount();
			} else {
				nextCursor = -1L;
			}
		} else {
			Long cursor = (lastBoardId == null) ? Long.MAX_VALUE : lastBoardId + 1;

			List<Board> boards = boardRepository.findBoardsByIdDesc(noTopic, toolId, cursor, size + 1);

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
		List<BoardResponse> boardResList = paginatedBoards.stream()
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
				return BoardResponse.of(board, toolName, toolLogo, commentCount, boardImages, isScrapped, savedToolid,
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

	private Board createToolBoard(final Tool tool, final BoardCreateAndUpdateRequest req, final User user) {
		Board board = Board.create(tool, user, req.title(), req.content());
		board = boardRepository.save(board);

		return board;
	}

	private Board createFreeBoard(final User user, final BoardCreateAndUpdateRequest req) {
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

	public User getUserById(final Long userId) {
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
		User user = getUser(userIdOrNull);

		List<Board> content = boards.getContent();
		List<Long> boardIds = content.stream().map(Board::getId).toList();
		// 스크랩 수는 배치 쿼리로 한 번에 조회 (N+1 방지)
		Map<Long, Long> scrapCountMap = boardIds.isEmpty()
			? Map.of() : boardScrapRepository.countMapByBoardIds(boardIds);
		// 댓글 수도 배치 쿼리로 한 번에 조회 (N+1 방지)
		Map<Long, Long> commentCountMap = boardIds.isEmpty()
			? Map.of() : commentRepository.countMapByBoardIds(boardIds);

		List<BoardResponse> boardResList = content.stream()
			.map(board -> {
				// 자유 게시판은 tool이 없으므로 toolId는 nullable
				Long savedToolId = board.getTool() != null ? board.getTool().getToolId() : null;
				String toolName = board.getTool() != null ? board.getTool().getToolMainName() : FREE;
				String toolLogo = board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO;
				int commentCount = commentCountMap.getOrDefault(board.getId(), 0L).intValue();
				List<String> images = boardImageService.getBoardImageUrls(board.getId());
				boolean isScraped = boardScrapService.isScraped(user, board);
				long scrapCount = scrapCountMap.getOrDefault(board.getId(), 0L);
				return BoardResponse.of(board, toolName, toolLogo, commentCount, images, isScraped, savedToolId,
					scrapCount);
			})
			.toList();

		PagenationDto pageInfo = PagenationDto.of(pageable.getPageNumber(), pageable.getPageSize(),
			boards.getTotalPages());

		return new BoardListResponse(boardResList, userIdOrNull, pageInfo);
	}

	public int getCommentCount(final Long boardId) {
		List<Comment> commentEntityList = commentRepository.findCommentsByBoardId(boardId);
		log.debug("댓글 Entity리스트를 받아옵니다 : {}", commentEntityList.size());
		return commentEntityList.size();
	}

	public User getUser(final Long userIdOrNull) {
		User user = null;
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
