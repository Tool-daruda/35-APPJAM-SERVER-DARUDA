package com.daruda.darudaserver.domain.community.service;

import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.util.ValidateBoard;
import com.daruda.darudaserver.domain.community.dto.req.BoardCreateAndUpdateReq;
import com.daruda.darudaserver.domain.community.dto.res.BoardRes;
import com.daruda.darudaserver.domain.community.dto.res.BoardScrapRes;
import com.daruda.darudaserver.domain.community.dto.res.GetBoardResponse;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.entity.BoardImage;
import com.daruda.darudaserver.domain.community.entity.BoardScrap;
import com.daruda.darudaserver.domain.community.repository.BoardImageRepository;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.community.repository.BoardScrapRepository;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.repository.ToolRepository;
import com.daruda.darudaserver.domain.user.dto.response.BoardListResponse;
import com.daruda.darudaserver.domain.user.dto.response.FavoriteBoardsResponse;
import com.daruda.darudaserver.domain.user.dto.response.FavoriteBoardsRetrieveResponse;
import com.daruda.darudaserver.domain.user.dto.response.PagenationDto;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.InvalidValueException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.error.exception.UnauthorizedException;
import com.daruda.darudaserver.global.image.service.ImageService;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.daruda.darudaserver.domain.community.entity.QBoard.board;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardImageService boardImageService;
    private final BoardImageRepository boardImageRepository;
    private final ImageService imageService;
    private final UserRepository userRepository;
    private final BoardScrapRepository boardScrapRepository;
    private final ToolRepository toolRepository;
    private final CommentRepository commentRepository;
    private final ValidateBoard validateBoard;

    private final String TOOL_LOGO = "https://daruda.s3.ap-northeast-2.amazonaws.com/Cursor_logo.png";
    private final String IMAGE_URL = "https://daruda.s3.ap-northeast-2.amazonaws.com/";
    private final String FREE = "자유";

    private final JPAQueryFactory jpaQueryFactory;
    // 게시판 생성
    public BoardRes createBoard(final Long userId, final BoardCreateAndUpdateReq boardCreateAndUpdateReq, final List<MultipartFile> images) {
        UserEntity user = getUserById(userId);
        Tool tool = getToolById(boardCreateAndUpdateReq.toolId());
        Board board = boardCreateAndUpdateReq.isFree() ?
                createFreeBoard(user, boardCreateAndUpdateReq) :
                createToolBoard(tool, boardCreateAndUpdateReq, user);

        // 이미지 처리
        List<String> imageUrls = processImages(board, images);
        List<String> boardImageUrls = imageUrls.stream()
                .map(url -> IMAGE_URL + url)
                .toList();

        // Tool 정보 설정
        String toolName = board.getTool() != null ? board.getTool().getToolMainName() : FREE;
        String toolLogo = board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO;

        return BoardRes.of(board, toolName, toolLogo, getCommentCount(board.getId()), boardImageUrls,tool.getToolId());
    }

    // 게시판 업데이트
    public BoardRes updateBoard(final Long userId, final Long boardId, final BoardCreateAndUpdateReq boardCreateAndUpdateReq, final List<MultipartFile> images) {
        Board board = validateBoardAndUser(userId, boardId);
        Tool tool = getToolById(boardCreateAndUpdateReq.toolId());
        UserEntity user = getUser(userId);
        board.update(
                tool,
                board.getUser(),
                boardCreateAndUpdateReq.title(),
                boardCreateAndUpdateReq.content(),
                boardCreateAndUpdateReq.isFree()
        );

        List<String> imageUrls = processImages(board, images);

        String toolName = board.getTool() != null ? board.getTool().getToolMainName() : FREE;
        String toolLogo = board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO;

        boolean isScrapped=false;
        BoardScrap boardScrap = boardScrapRepository.findByUserAndBoard(user.getId(), board.getId())
                .orElse(null);

        if(boardScrap!=null){
            isScrapped = !boardScrap.isDelYn();
        }

        return BoardRes.of(board, toolName, toolLogo, getCommentCount(board.getId()), imageUrls, isScrapped);
    }

    // 게시판 삭제
    public void deleteBoard(final Long userId, final Long boardId) {
        Board board = validateBoardAndUser(userId, boardId);
        deleteOriginImages(boardId);
        List<CommentEntity> commentEntityList = commentRepository.findAllByBoardId(boardId);
        List<BoardScrap> scraps = boardScrapRepository.findAllByBoardId(boardId);
        if (!scraps.isEmpty()) {
            boardScrapRepository.deleteAll(scraps);
            log.info("삭제된 게시글과 연관된 스크랩 데이터를 제거했습니다. Scrap Count: {}", scraps.size());
        }
        board.delete();
    }

    // 스크랩 처리
    public BoardScrapRes postScrap(final Long userId, final Long boardId) {
        UserEntity user = getUserById(userId);
        Board board = getBoardById(boardId);

        BoardScrap boardScrap = boardScrapRepository.findByUserAndBoard(user.getId(), board.getId()).orElse(null);

        if (boardScrap == null) {
            boardScrap = BoardScrap.builder().user(user).board(board).build();
            boardScrapRepository.save(boardScrap);
        } else {
            boardScrap.update();
        }
        return BoardScrapRes.of(boardId, !boardScrap.isDelYn());
    }

    // 게시판 조회
    public BoardRes getBoard(final Long userIdOrNull, final Long boardId) {
        UserEntity user = getUser(userIdOrNull);
        Board board = getBoardById(boardId);
        Long toolId = getToolId(boardId);
        List<String> imageUrls = boardImageService.getBoardImageUrls(boardId);

        String toolName = board.getTool() != null ? board.getTool().getToolMainName() : FREE;
        String toolLogo = board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO;
        Boolean isScraped = getBoardScrap(user, board);
        return BoardRes.of(board, toolName, toolLogo, getCommentCount(boardId), imageUrls, isScraped, toolId);
    }

    // 내가 쓴  게시판 조회
    public BoardRes getMyBoard(final UserEntity user,final  Long boardId) {
        Board board = getBoardById(boardId);
        List<String> imageUrls = boardImageService.getBoardImageUrls(boardId);
        String toolName = board.getTool() != null ? board.getTool().getToolMainName() : FREE;
        String toolLogo = board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO;

        Boolean isScraped = getBoardScrap(user, board);
        return BoardRes.of(board, toolName, toolLogo, getCommentCount(boardId), imageUrls, isScraped);
    }

    public GetBoardResponse getBoardList(final Long userIdOrNull, final Boolean noTopic, final Long toolId, final int size, final Long lastBoardId) {

        log.info("USERID OR NULL " + userIdOrNull);
        Long cursor = (lastBoardId == null) ? Long.MAX_VALUE : lastBoardId+1;
        PageRequest pageRequest = PageRequest.of(0, size + 1);
        UserEntity user = getUser(userIdOrNull);
        log.info("USER : " + user);

        //NoTopic = null, toolId = null -> 전체 게시판 조회
        //NoTopic = False , toolId != null -> 툴 게시판
        //NoTopic = True , toolId == null -> 자유게시판
        if(noTopic != null) {
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
                .fetchOne()).orElse(0L);

        // Cursor 기반 페이징을 적용한 게시글 목록 가져오기
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

        // 다음 페이지 여부 확인
        boolean hasNextPage = boards.size() > size;
        List<Board> paginatedBoards = hasNextPage ? boards.subList(0, size) : boards;

        // nextCursor
        long nextCursor = hasNextPage ? boards.get(size).getId() : -1L;

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
                    boolean isScrapped = getBoardScrap(user, board);
                    return BoardRes.of(board, toolName, toolLogo, commentCount, boardImages, isScrapped,savedToolid);
                })
                .toList();

        ScrollPaginationDto scrollPaginationDto = ScrollPaginationDto.of(totalElements, nextCursor);
        return new GetBoardResponse(boardResList, scrollPaginationDto);
    }



    public FavoriteBoardsRetrieveResponse getFavoriteBoards(final Long userId, final Pageable pageable){
        validateBoard.validateUser(userId);

        Page<BoardScrap> boardScraps = boardScrapRepository.findAllActiveByUserId(userId, pageable);
        List<FavoriteBoardsResponse> favoriteBoardsResponses = boardScraps.getContent().stream()
                .filter(boardScrap -> !boardScrap.isDelYn()) // 스크랩 데이터의 삭제 여부 체크
                .map(boardScrap -> {
                    Board board = boardScrap.getBoard();
                    if (board.isDelYn()) { // 삭제된 게시판인지 확인
                        return null; // 삭제된 게시판은 제외
                    }
                    return FavoriteBoardsResponse.builder()
                            .boardId(board.getId())
                            .title(board.getTitle())
                            .content(board.getContent())
                            .updatedAt(board.getUpdatedAt())
                            .toolName(freeName(board))
                            .toolLogo(freeLogo(board))
                            .isScrapped(!boardScrap.isDelYn())
                            .build();
                })
                .filter(Objects::nonNull) // null 값 제외
                .toList();

        PagenationDto pageInfo = PagenationDto.of(pageable.getPageNumber(), pageable.getPageSize(), boardScraps.getTotalPages());
        return  new FavoriteBoardsRetrieveResponse(userId, favoriteBoardsResponses, pageInfo);

    }

    private Board validateBoardAndUser(final Long userId, final Long boardId) {
        Board board = getBoardById(boardId);
        if (!board.getUser().getId().equals(userId)) {
            log.debug("게시판 작성자와, 유저가 다릅니다.");
            throw new UnauthorizedException(ErrorCode.BOARD_FORBIDDEN);
        }
        return board;
    }

    private List<String> processImages(final Board board, final List<MultipartFile> images) {
        if (images == null || images.isEmpty() || images.stream().allMatch(MultipartFile::isEmpty)) {
            deleteOriginImages(board.getId());
            return List.of();
        }
        deleteOriginImages(board.getId());
        List<Long> imageIds = imageService.uploadImages(images);
        boardImageService.saveBoardImages(board.getId(), imageIds);
        return boardImageService.getBoardImageUrls(board.getId());
    }

    private Board createToolBoard(final Tool tool,final BoardCreateAndUpdateReq req, final UserEntity user) {
        return boardRepository.save(Board.create( tool, user, req.title(), req.content()));
    }

    private Board createFreeBoard(final UserEntity user, final BoardCreateAndUpdateReq req) {
        return boardRepository.save(Board.createFree(user, req.title(), req.content()));
    }

    private Board getBoardById(final Long boardId) {
        return boardRepository.findByIdAndDelYn(boardId, false)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));
    }

    private Tool getToolById(final Long toolId) {
        return toolRepository.findById(toolId).orElseThrow(() -> new NotFoundException(ErrorCode.TOOL_NOT_FOUND));
    }

    private UserEntity getUserById(final Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private void deleteOriginImages(final Long boardId) {
        List<BoardImage> boardImages = boardImageRepository.findAllByBoardId(boardId);
        List<Long> imageIds = boardImages.stream().map(BoardImage::getImageId).toList();
        boardImageRepository.deleteAll(boardImages);
        imageService.deleteImages(imageIds);
    }

    public BoardListResponse getMyBoards(Long userIdOrNull, Pageable pageable){
        validateBoard.validateUser(userIdOrNull);
        log.debug("사용자를 조회합니다, {}", userIdOrNull);
        Page<Board> boards = boardRepository.findAllByUserIdAndDelYnFalse(userIdOrNull, pageable);
        UserEntity user = getUser(userIdOrNull);
        List<BoardRes> boardResList = boards.getContent().stream()
                .map(board -> getMyBoard( user, board.getId()))
                .toList();

        PagenationDto pageInfo = PagenationDto.of(pageable.getPageNumber(), pageable.getPageSize(), boards.getTotalPages());

        return new BoardListResponse(boardResList, userIdOrNull, pageInfo);
    }

    public int getCommentCount(final Long boardId){
        List<CommentEntity> commentEntityList = commentRepository.findAllByBoardId(boardId);
        log.debug("댓글 Entity리스트를 받아옵니다 : " + commentEntityList.size());
        return commentEntityList.size();
    }

    public Boolean getBoardScrap(final UserEntity user, final Board board){
        if (user == null) {
            log.info("** Board : " + board.getId() + " 스크랩 여부 : false (비로그인 사용자)");
            return false;
        }
        boolean isScrapped = boardScrapRepository.findByUserAndBoard(user.getId(), board.getId())
                .map(BoardScrap::isDelYn)
                .map(delYn -> !delYn)
                .orElse(false);

        log.info("** Board : " + board.getId() + " 스크랩 여부 :"+isScrapped);
        return isScrapped;
    }

    public UserEntity getUser(final Long userIdOrNull) {
        UserEntity user = null;
        if (userIdOrNull != null) {
            Long userId = userIdOrNull;
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
            log.debug("유저 정보를 조회했습니다: {}", user.getId());
        }
        return user;
    }

    public Long getToolId(Long boardId){
        Board board = getBoardById(boardId);
        Long toolId = board.isFree() ? null : board.getTool().getToolId();
        return toolId;
    }

    public String freeName(Board board) {
        return board.getTool() != null ? board.getTool().getToolMainName() : FREE;
    }
    public String freeLogo(Board board){
        return board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO;
    }
}
