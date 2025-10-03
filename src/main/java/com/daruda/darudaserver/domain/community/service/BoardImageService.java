package com.daruda.darudaserver.domain.community.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.community.entity.BoardImage;
import com.daruda.darudaserver.domain.community.repository.BoardImageRepository;
import com.daruda.darudaserver.global.image.service.ImageService;

import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class BoardImageService {

	private static final String IMAGE_URL = "https://d10hjhy1a1c8eq.cloudfront.net/";

	private final BoardImageRepository boardImageRepository;
	private final ImageService imageService;

	public void saveBoardImages(Long boardId, List<Long> imageIds) {
		imageIds.forEach(imageId -> {
			BoardImage boardImage = BoardImage.create(boardId, imageId);
			boardImageRepository.save(boardImage);
		});
	}

	public List<String> getBoardImageUrls(Long boardId) {
		return boardImageRepository.findAllByBoardId(boardId).stream()
			.map(boardImage -> imageService.getImageUrlById(boardImage.getImageId()))
			.toList();
	}

}
