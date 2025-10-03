package com.daruda.darudaserver.domain.community;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardImageRepository;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.community.service.BoardImageService;
import com.daruda.darudaserver.domain.community.service.BoardService;
import com.daruda.darudaserver.global.image.service.ImageService;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

	@InjectMocks
	private BoardService boardService;

	@Mock
	private ImageService imageService;

	@Mock
	private BoardImageService boardImageService;

	@Mock
	private BoardRepository boardRepository;

	@Mock
	private BoardImageRepository boardImageRepository;

	@Mock
	private Board board;

	@Test
	@DisplayName("processImages: 이미지가 있을 때 정상적으로 처리되는지 테스트")
	void processImages_WithImages_Success() throws Exception {
		// given
		Long boardId = 1L;
		List<String> imageList = List.of("image1.png", "image2.png");
		List<Long> imageIds = List.of(10L, 20L);
		List<String> imageUrls = List.of("url1", "url2");

		Mockito.when(board.getId()).thenReturn(boardId);
		Mockito.when(imageService.createImage(imageList)).thenReturn(imageIds);
		Mockito.when(boardImageService.getBoardImageUrls(boardId)).thenReturn(imageUrls);

		java.lang.reflect.Method method = BoardService.class.getDeclaredMethod("processImages", Board.class,
			List.class);
		method.setAccessible(true);

		// when
		@SuppressWarnings("unchecked")
		List<String> result = (List<String>)method.invoke(boardService, board, imageList);

		// then
		Mockito.verify(imageService).createImage(imageList);
		Mockito.verify(boardImageService).saveBoardImages(boardId, imageIds);
		Mockito.verify(boardImageService).getBoardImageUrls(boardId);
		Assertions.assertEquals(imageUrls, result);
	}
}

