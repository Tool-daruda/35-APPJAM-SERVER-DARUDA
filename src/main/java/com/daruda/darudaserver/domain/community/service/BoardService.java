package com.daruda.darudaserver.domain.community.service;

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
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.common.response.ScrollPaginationCollection;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.error.exception.UnauthorizedException;
import com.daruda.darudaserver.global.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

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

    private final String TOOL_LOGO = "ToolLogo.jpeg";

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
        String toolName = board.getTool() != null ? board.getTool().getToolMainName() : "자유";
        String toolLogo = board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO;

        return BoardRes.of(board, toolName, toolLogo, getCommentCount(board.getBoardId()), imageUrls);
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

        String toolName = board.getTool() != null ? board.getTool().getToolMainName() : "자유";
        String toolLogo = board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO;

        return BoardRes.of(board, toolName, toolLogo, getCommentCount(board.getBoardId()), imageUrls);
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
        String toolName = board.getTool() != null ? board.getTool().getToolMainName() : "자유";
        String toolLogo = board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO;

        return BoardRes.of(board, toolName, toolLogo, getCommentCount(boardId), imageUrls);
    }

    // 게시판 리스트 조회
    public GetBoardResponse getBoardList(final Long toolId, final int size, final Long lastBoardId) {

        List<Board> boards;
        Long cursor = (lastBoardId == null) ? Long.MAX_VALUE : lastBoardId;
        PageRequest pageRequest = PageRequest.of(0, size + 1);

        // 전체 조회
        if (toolId == null) {
            boards = boardRepository.findByBoardIdLessThanOrderByBoardIdDesc(cursor, pageRequest);
        }
        // 자유 게시판 조회
        else if (toolId == -1) {
            boards = boardRepository.findByIsFreeAndBoardIdLessThanOrderByBoardIdDesc(true,cursor, pageRequest);
        }
        // 특정 Tool 게시판 조회
        else {
            Tool tool = getToolById(toolId);
            boards = boardRepository.findByToolAndBoardIdLessThanOrderByBoardIdDesc(tool, cursor, pageRequest);
        }

        ScrollPaginationCollection<Board> boardsCursor = ScrollPaginationCollection.of(boards, size);

        List<BoardRes> boardResList = boardsCursor.getCurrentScrollItems().stream()
                .map(board -> BoardRes.of(
                        board,
                        board.getTool() != null ? board.getTool().getToolMainName() : "자유",
                        board.getTool() != null ? board.getTool().getToolLogo() : TOOL_LOGO,
                        getCommentCount(board.getBoardId()),
                        boardImageService.getBoardImageUrls(board.getBoardId())
                ))
                .collect(Collectors.toList());
        log.info("BoardRes List: {}", boardResList);
        long nextCursor = boardsCursor.isLastScroll() ? -1L : boardsCursor.getNextCursor().getBoardId();

        return new GetBoardResponse(boardResList, boardsCursor.getTotalElements(), nextCursor);
    }

    private Board validateBoardAndUser(final Long userId, final Long boardId) {
        Board board = getBoardById(boardId);
        if (!board.getUser().getId().equals(userId)) {
            throw new UnauthorizedException(ErrorCode.BOARD_FORBIDDEN);
        }
        return board;
    }

    private List<String> processImages(final Board board, final List<MultipartFile> images) {
        if (images == null || images.isEmpty() || images.stream().allMatch(MultipartFile::isEmpty)) {
            deleteOriginImages(board.getBoardId());
            return List.of();
        }
        deleteOriginImages(board.getBoardId());
        List<Long> imageIds = imageService.uploadImages(images);
        boardImageService.saveBoardImages(board.getBoardId(), imageIds);
        return boardImageService.getBoardImageUrls(board.getBoardId());
    }

    private int getCommentCount(final Long boardId) {
        return 1; // Mock 데이터
    }

    private Board createToolBoard(final Tool tool,final BoardCreateAndUpdateReq req, final UserEntity user) {
        return boardRepository.save(Board.create( tool, user, req.title(), req.content()));
    }

    private Board createFreeBoard(final UserEntity user, final BoardCreateAndUpdateReq req) {
        return boardRepository.save(Board.createFree(user, req.title(), req.content()));
    }

    private Board getBoardById(final Long boardId) {
        return boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND));
    }

    private Tool getToolById(final Long toolId) {
        return toolRepository.findById(toolId).orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND));
    }


    private UserEntity getUserById(final Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND));
    }

    private void deleteOriginImages(final Long boardId) {
        List<BoardImage> boardImages = boardImageRepository.findAllByBoardId(boardId);
        List<Long> imageIds = boardImages.stream().map(BoardImage::getImageId).toList();
        boardImageRepository.deleteAll(boardImages);
        imageService.deleteImages(imageIds);
    }
}
