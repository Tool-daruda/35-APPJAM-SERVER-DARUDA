package com.daruda.darudaserver.domain.community.service;

import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
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
import com.daruda.darudaserver.domain.user.dto.response.PagenationDto;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.domain.user.service.UserService;
import com.daruda.darudaserver.global.common.response.ScrollPaginationCollection;
import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.InvalidValueException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.error.exception.UnauthorizedException;
import com.daruda.darudaserver.global.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    private final UserService userService;
    private final CommentRepository commentRepository;

    private final String TOOL_LOGO = "https://daruda.s3.ap-northeast-2.amazonaws.com/daruda+logo.svg";
    private final String FREE = "자유";

    // 게시판 생성
    public BoardRes createBoard(final Long userId, final BoardCreateAndUpdateReq boardCreateAndUpdateReq, final List<MultipartFile> images) {
        UserEntity user = getUserById(userId);
        Tool tool = getToolById(boardCreateAndUpdateReq.toolId());
        Board board = boardCreateAndUpdateReq.isFree() ?
                createFreeBoard(user, boardCreateAndUpdateReq) :
                createToolBoard(tool, boardCreateAndUpdateReq, user);

        // 이미지 처리
        List<String> imageUrls = processImages(board, images);

        // Tool 정보 설정
        String toolName = board.getTool() != null ? board.getTool().getToolMainName() : FREE;
        String toolLogo = board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO;

        return BoardRes.of(board, toolName, toolLogo, getCommentCount(board.getId()), imageUrls);
    }

    // 게시판 업데이트
    public BoardRes updateBoard(final Long userId, final Long boardId, final BoardCreateAndUpdateReq boardCreateAndUpdateReq, final List<MultipartFile> images) {
        Board board = validateBoardAndUser(userId, boardId);
        Tool tool = getToolById(boardCreateAndUpdateReq.toolId());
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

        return BoardRes.of(board, toolName, toolLogo, getCommentCount(board.getId()), imageUrls);
    }

    // 게시판 삭제
    public void deleteBoard(final Long userId, final Long boardId) {
        Board board = validateBoardAndUser(userId, boardId);
        deleteOriginImages(boardId);
        board.delete();
    }

    // 스크랩 처리
    public BoardScrapRes postScrap(final Long userId, final Long boardId) {
        UserEntity user = getUserById(userId);
        Board board = getBoardById(boardId);

        BoardScrap boardScrap = boardScrapRepository.findByUserAndBoard(user, board).orElse(null);

        if (boardScrap == null) {
            boardScrap = BoardScrap.builder().user(user).board(board).build();
            boardScrapRepository.save(boardScrap);
        } else {
            boardScrap.update();
        }
        return BoardScrapRes.of(boardId, !boardScrap.isDelYn());
    }

    // 게시판 조회
    public BoardRes getBoard(final Long boardId) {
        Board board = getBoardById(boardId);
        List<String> imageUrls = boardImageService.getBoardImageUrls(boardId);
        String toolName = board.getTool() != null ? board.getTool().getToolMainName() : FREE;
        String toolLogo = board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO;

        return BoardRes.of(board, toolName, toolLogo, getCommentCount(boardId), imageUrls);
    }

    // 게시판 리스트 조회
    public GetBoardResponse getBoardList(final Boolean isFree,final Long toolId, final int size, final Long lastBoardId) {

        List<Board> boards;
        Long cursor = (lastBoardId == null) ? Long.MAX_VALUE : lastBoardId;
        PageRequest pageRequest = PageRequest.of(0, size + 1);

        // 전체 조회
        if(Boolean.TRUE.equals(isFree)){
            log.info("자유 게시판을 조회합니다");
            boards = boardRepository.findBoards(null, true, cursor, pageRequest);
        }
        // 특정 Tool 게시판 조회
        else if (toolId != null) {
            Tool tool = getToolById(toolId);
            log.info(tool.getToolMainName() + " 게시판을 조회합니다");
            boards = boardRepository.findBoards(tool, false, cursor,pageRequest);
        }
        //전체 게시판 조회
        else{
            log.info("전체 게시판을 조회합니다");
            boards = boardRepository.findBoards(null, null, cursor, pageRequest);

        }

        ScrollPaginationCollection<Board> boardsCursor = ScrollPaginationCollection.of(boards, size);

        List<BoardRes> boardResList = boardsCursor.getCurrentScrollItems().stream()
                .map(board -> BoardRes.of(
                        board,
                        board.getTool() != null ? board.getTool().getToolMainName() : FREE,
                        board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO,
                        getCommentCount(board.getId()),
                        boardImageService.getBoardImageUrls(board.getId())
                ))
                .toList();
        log.debug("BoardRes List: {}", boardResList);
        long nextCursor = boardsCursor.isLastScroll() ? -1L : boardsCursor.getNextCursor().getId();

        ScrollPaginationDto scrollPaginationDto = ScrollPaginationDto.of(boardsCursor.getTotalElements(), nextCursor);
        return new GetBoardResponse(boardResList,scrollPaginationDto);
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

    public BoardListResponse getMyBoards(Long userId, Pageable pageable){
        userService.validateUser(userId);
        Page<Board> boards = boardRepository.findAllByUserIdAndDelYnFalse(userId, pageable);

        List<BoardRes> boardResList = boards.getContent().stream()
                .map(board -> getBoard(board.getId()))
                .toList();

        PagenationDto pageInfo = PagenationDto.of(pageable.getPageNumber(), pageable.getPageSize(), boards.getTotalPages());

        return new BoardListResponse(boardResList, userId, pageInfo);

    }

    public int getCommentCount(final Long boardId){
        List<CommentEntity> commentEntityList = commentRepository.findAllByBoardId(boardId);
        log.debug("댓글 Entity리스트를 받아옵니다 : " + commentEntityList.size());
        return commentEntityList.size();
    }

}
