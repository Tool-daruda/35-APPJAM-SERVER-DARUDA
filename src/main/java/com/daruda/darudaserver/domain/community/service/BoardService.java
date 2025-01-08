package com.daruda.darudaserver.domain.community.service;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardImageService boardImageService;
    private final ImageService imageService;

    public BoardRes createBoard(
                            final BoardCreateAndUpdateReq boardCreateAndUpdateReq,
                            List<MultipartFile> images) {
        //userId 검증
        Long userId= 1L;
        //toolId 검증

        //Board 저장
        Board board = createBoard(userId,  boardCreateAndUpdateReq);
        // imageURL 반환
        List<Long> imageIds =imageService.uploadImages(images,"board");
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
        List<Long> imageIds =imageService.uploadImages(images,"board");
        // BoardImage 저장
        boardImageService.saveBoardImages(board.getBoardId(), imageIds);
        List<String> imageUrls= boardImageService.getBoardImageUrls(board.getBoardId());
        return BoardRes.of(board,imageUrls);
    }

    private Board createBoard( final Long userId, final BoardCreateAndUpdateReq boardCreateAndUpdateReq){
        Board board = Board.create(boardCreateAndUpdateReq.toolId(), userId,
                boardCreateAndUpdateReq.title(), boardCreateAndUpdateReq.content()
                );
       return boardRepository.save(board);
    }

    private Board updateBoard( final Long boardId, final Long userId, final BoardCreateAndUpdateReq boardCreateAndUpdateReq){
        Board board = Board.update(boardId , boardCreateAndUpdateReq.toolId(), userId,
                boardCreateAndUpdateReq.title(), boardCreateAndUpdateReq.content()
        );
        return boardRepository.save(board);
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

    private Board getBoardById(final Long boardId){
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND));
    }
}
