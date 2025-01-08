package com.daruda.darudaserver.domain.community.service;

import com.daruda.darudaserver.domain.community.dto.request.BoardCreateAndUpdateReq;
import com.daruda.darudaserver.domain.community.dto.response.BoardRes;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.global.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final ImageService imageService;
    public BoardRes createBoard(
                            final BoardCreateAndUpdateReq boardCreateAndUpdateReq,
                            List<MultipartFile> images) {
        //userId 검증
        Long userId= 1L;
        //toolId 검증
        List<String> imageUrls= imageService.uploadImages(images,"board");
        Board board = saveBoard(userId,  boardCreateAndUpdateReq);
        return BoardRes.of(board,imageUrls);
    }

    private Board saveBoard( final Long userId, final BoardCreateAndUpdateReq boardCreateAndUpdateReq){
        Board board = Board.create(boardCreateAndUpdateReq.toolId(), userId,
                boardCreateAndUpdateReq.title(), boardCreateAndUpdateReq.content()
                );
       return boardRepository.save(board);
    }

}
