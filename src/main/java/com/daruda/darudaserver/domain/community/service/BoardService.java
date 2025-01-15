package com.daruda.darudaserver.domain.community.service;

import com.daruda.darudaserver.domain.community.dto.req.BoardCreateAndUpdateReq;
import com.daruda.darudaserver.domain.community.dto.res.BoardRes;
import com.daruda.darudaserver.domain.community.dto.res.BoardScrapRes;
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
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.error.exception.UnauthorizedException;
import com.daruda.darudaserver.global.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ToolRepository toolRepository;
    private final UserRepository userRepository;
    private final BoardScrapRepository boardScrapRepository;

    private final String TOOL_LOGO = "ToolLogo.jpeg";

    // 게시판 생성
    public BoardRes createBoard(final Long userId,final  BoardCreateAndUpdateReq boardCreateAndUpdateReq, final List<MultipartFile> images) {
        UserEntity user = getUserById(userId);
        Board board = boardCreateAndUpdateReq.isFree() ?
                createFreeBoard(user, boardCreateAndUpdateReq) :
                createToolBoard(boardCreateAndUpdateReq, user);

        // 이미지 처리
        List<String> imageUrls = processImages(board, images);

        // Tool 정보 설정
        String toolName = boardCreateAndUpdateReq.isFree() ? "자유" : getToolName(board.getToolId());
        String toolLogo = boardCreateAndUpdateReq.isFree() ? TOOL_LOGO : getToolLogo(board.getToolId());

        return BoardRes.of(board, toolName, toolLogo, getCommentCount(board.getBoardId()), imageUrls );
    }

    // 게시판 업데이트
    public BoardRes updateBoard(final Long userId,final  Long boardId, final BoardCreateAndUpdateReq boardCreateAndUpdateReq, final List<MultipartFile> images) {
        Board board = validateBoardAndUser(userId, boardId);

        board.update(
                boardCreateAndUpdateReq.toolId(),
                board.getUser(),
                boardCreateAndUpdateReq.title(),
                boardCreateAndUpdateReq.content(),
                boardCreateAndUpdateReq.isFree()
        );

        List<String> imageUrls = processImages(board, images);

        String toolName = boardCreateAndUpdateReq.isFree() ? "자유" : getToolName(board.getToolId());
        String toolLogo = boardCreateAndUpdateReq.isFree() ? TOOL_LOGO : getToolLogo(board.getToolId());

        return BoardRes.of(board, toolName, toolLogo,  getCommentCount(boardId),imageUrls);
    }

    // 게시판 삭제
    public void deleteBoard(final Long userId,final  Long boardId) {
        Board board = validateBoardAndUser(userId, boardId);
        deleteOriginImages(boardId);
        board.delete();
    }

    // 스크랩 처리
    public BoardScrapRes postScrap(final Long userId,final Long boardId) {
        UserEntity user = getUserById(userId);
        Board board = getBoardById(boardId);

        // 저장되어있는 BoardScrap 이 있으면 delete(). 없으면 생성
        BoardScrap boardScrap = boardScrapRepository.findByUserAndBoard(user, board)
                .orElse( boardScrap = null);

        // 이미 존재하는 경우 - SoftDelete
        if (boardScrap == null) {
            boardScrap = BoardScrap.builder().user(user).board(board).build();
            boardScrapRepository.save(boardScrap);
        }else{
            boardScrap.update();
        }
        return BoardScrapRes.of(boardId, !boardScrap.isDelYn());

    }

    // 게시판 조회
    public BoardRes getBoard(final Long boardId) {
        Board board = getBoardById(boardId);
        List<String> imageUrls = boardImageService.getBoardImageUrls(boardId);
        String toolName = board.isFree() ? "자유" : getToolName(board.getToolId());
        String toolLogo = board.isFree() ? TOOL_LOGO : getToolLogo(board.getToolId());

        return BoardRes.of(board, toolName, toolLogo,  getCommentCount(boardId),imageUrls);
    }

    // 유효성 검증
    private Board validateBoardAndUser(final Long userId, final Long boardId) {
        Board board = getBoardById(boardId);
        if (!board.getUser().getId().equals(userId)) {
            throw new UnauthorizedException(ErrorCode.BOARD_FORBIDDEN);
        }
        return board;
    }

    // 이미지 처리
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

    // 댓글 개수 반환 (Mock)
    private int getCommentCount(final Long boardId) {

        return 1;
    }

    // Board 및 Tool 생성
    private Board createToolBoard(final BoardCreateAndUpdateReq req, final UserEntity user) {
        return boardRepository.save(Board.create(req.toolId(), user, req.title(), req.content()));
    }

    private Board createFreeBoard(final UserEntity user, final BoardCreateAndUpdateReq req) {
        return boardRepository.save(Board.createFree(user, req.title(), req.content()));
    }

    // Tool 정보 가져오기
    private String getToolName(final Long toolId) {
        return getToolById(toolId).getToolMainName();
    }

    private String getToolLogo(final Long toolId) {
        return getToolById(toolId).getToolLogo();
    }

    // 도구 조회
    private Tool getToolById(final Long toolId) {
        return toolRepository.findById(toolId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND));
    }

    // 유저 및 게시판 조회
    private Board getBoardById(final Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND));
        if (board.isDelYn()) {
            throw new NotFoundException(ErrorCode.DATA_NOT_FOUND);
        }
        return board;
    }

    private UserEntity getUserById(final Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND));
    }

    // 기존 이미지 삭제
    private void deleteOriginImages(final Long boardId) {
        List<BoardImage> boardImages = boardImageRepository.findAllByBoardId(boardId);
        List<Long> imageIds = boardImages.stream().map(BoardImage::getImageId).toList();
        boardImageRepository.deleteAll(boardImages);
        imageService.deleteImages(imageIds);
    }
}
