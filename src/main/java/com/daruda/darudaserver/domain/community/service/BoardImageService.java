package com.daruda.darudaserver.domain.community.service;

import com.daruda.darudaserver.domain.community.entity.BoardImage;
import com.daruda.darudaserver.domain.community.repository.BoardImageRepository;
import com.daruda.darudaserver.global.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Transactional
@Service
@RequiredArgsConstructor
public class BoardImageService {

    private final BoardImageRepository boardImageRepository;
    private final ImageService imageService;
    private final String IMAGE_URL = "https://daruda.s3.ap-northeast-2.amazonaws.com/";

    public void saveBoardImages(Long boardId, List<Long> imageIds){
        imageIds.forEach(imageId ->{
            BoardImage boardImage = BoardImage.create(boardId, imageId);
            boardImageRepository.save(boardImage);
        });
    }

    public List<String> getBoardImageUrls(Long boardId){
        return boardImageRepository.findAllByBoardId(boardId).stream()
                .map(boardImage->
                       IMAGE_URL +  imageService.getImageUrlById(boardImage.getImageId())
                )
                .toList();
    }

}
