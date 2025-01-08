package com.daruda.darudaserver.domain.community.service;

import com.daruda.darudaserver.domain.community.entity.BoardImage;
import com.daruda.darudaserver.domain.community.repository.BoardImageRepository;
import com.daruda.darudaserver.global.image.entity.Image;
import com.daruda.darudaserver.global.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardImageService {

    private final BoardImageRepository boardImageRepository;
    private final ImageService imageService;

    public void saveBoardImages(Long boardId, List<Long> imageIds){
        imageIds.forEach(imageId ->{
            BoardImage boardImage = BoardImage.create(boardId, imageId);
            boardImageRepository.save(boardImage);
        });
    }

    public List<String> getBoardImageUrls(Long boardId){
        return boardImageRepository.findAllByBoardId(boardId).stream()
                .map(boardImage->
                        imageService.getImageUrlById(boardImage.getImageId())
                )
                .toList();
    }

}
