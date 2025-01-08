package com.daruda.darudaserver.domain.community.service;

import com.daruda.darudaserver.domain.community.dto.request.BoardCreateAndUpdateRequest;
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
    public void createBoard(
                            final BoardCreateAndUpdateRequest boardCreateAndUpdateReq,
                            List<MultipartFile> images) {
        //userId 검증
        Long userId= 1L;
        //toolId 검증
        imageService.uploadImages(images,"board");
        saveBoard(userId,  boardCreateAndUpdateReq);
    }



    private void saveBoard( final Long userId, final BoardCreateAndUpdateRequest boardCreateAndUpdateReq){
        Board board = Board.create(boardCreateAndUpdateReq.toolId(), userId,
                boardCreateAndUpdateReq.title(), boardCreateAndUpdateReq.content()
                );
       boardRepository.save(board);
    }

}
