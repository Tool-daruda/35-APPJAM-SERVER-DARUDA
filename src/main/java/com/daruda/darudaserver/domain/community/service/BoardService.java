package com.daruda.darudaserver.domain.community.service;

import com.daruda.darudaserver.domain.community.dto.request.BoardCreateAndUpdateReq;
import com.daruda.darudaserver.domain.community.dto.response.BoardRes;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.repository.ToolRepository;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.daruda.darudaserver.domain.community.entity.Board.createFree;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardImageService boardImageService;
    private final ImageService imageService;
    private final ToolRepository toolRepository;
    private final UserRepository userRepository;

    public BoardRes createBoard(
            final Long userId,
            final BoardCreateAndUpdateReq boardCreateAndUpdateReq,
            List<MultipartFile> images) {

        //UserId 검증
        UserEntity user = getUserById(userId);
        Board board;

        // 자유 게시판일 경우
        if(boardCreateAndUpdateReq.isFree()){
            board = createFreeBoard(user.getUserId(), boardCreateAndUpdateReq);
        }
        // 툴 기반 게시판 생성
        else {
            Tool tool = getToolById(boardCreateAndUpdateReq.toolId());
            board = createToolBoard(tool.getToolId(), user.getUserId(), boardCreateAndUpdateReq);
        }
        // 이미지가 없으면 바로 응답 반환
        if (images == null || images.isEmpty() || images.stream().allMatch(MultipartFile::isEmpty)) {
            return BoardRes.of(board);
        }
        // imageURL 반환
        List<Long> imageIds =imageService.uploadImages(images);
        // BoardImage 저장
        boardImageService.saveBoardImages(board.getBoardId(), imageIds);
        List<String> imageUrls= boardImageService.getBoardImageUrls(board.getBoardId());
        return BoardRes.of(board,imageUrls);
    }

    public BoardRes updateBoard(
            final Long boardId,
            final BoardCreateAndUpdateReq boardCreateAndUpdateReq,
            List<MultipartFile> images) {
        //userId 검증
        Long userId= 1L;
        //toolId 검증

        //Board 객체 검증
        getBoardById(boardId);
        //Board 저장
        Board board = updateBoard(boardId , userId,boardCreateAndUpdateReq);
        // imageURL 반환
        List<Long> imageIds =imageService.uploadImages(images);
        // BoardImage 저장
        boardImageService.saveBoardImages(board.getBoardId(), imageIds);
        List<String> imageUrls= boardImageService.getBoardImageUrls(board.getBoardId());
        return BoardRes.of(board,imageUrls);
    }

    public void deleteBoard(final Long boardId) {
        Board board = getBoardById(boardId);
        board.delete();
        boardRepository.save(board);
    }

    public BoardRes getBoard(Long boardId) {
        Board board = getBoardById(boardId);
        List<String> imageUrls= boardImageService.getBoardImageUrls(board.getBoardId());
        return BoardRes.of(board,imageUrls);
    }

    private Board createToolBoard(final Long toolId, final Long userId, final BoardCreateAndUpdateReq boardCreateAndUpdateReq) {
        Board board = Board.create(toolId, userId, boardCreateAndUpdateReq.title(), boardCreateAndUpdateReq.content());
        return boardRepository.save(board);
    }

    private Board createFreeBoard(final Long userId, final BoardCreateAndUpdateReq boardCreateAndUpdateReq) {
        Board board = createFree(userId, boardCreateAndUpdateReq.title(), boardCreateAndUpdateReq.content());
        return boardRepository.save(board);
    }

    private Board updateBoard( final Long boardId, final Long userId, final BoardCreateAndUpdateReq boardCreateAndUpdateReq){
        Board board = Board.update(boardId , boardCreateAndUpdateReq.toolId(), userId,
                boardCreateAndUpdateReq.title(), boardCreateAndUpdateReq.content()
        );
        return boardRepository.save(board);
    }


    private Board getBoardById(final Long boardId){
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND));
    }

    private Tool getToolById(final Long toolId) {
        log.debug("툴을 조회합니다. toolId={}", toolId);
        return toolRepository.findById(toolId)
                .orElseThrow(() -> {
                    log.error("툴을 찾을 수 없습니다. toolId={}", toolId);
                    return new NotFoundException(ErrorCode.DATA_NOT_FOUND);
                });
    }

    public UserEntity getUserById(final Long userId) {
        log.debug("유저를 조회합니다. userId={}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("유저를 찾을 수 없습니다. userId={}", userId);
                    return new NotFoundException(ErrorCode.DATA_NOT_FOUND);
                });
    }
}
