package com.daruda.darudaserver.domain.community;

/*
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
	void processImages_WithImages_Success() {
		// given
		Long boardId = 1L;
		List<String> imageList = List.of("image1.png", "image2.png");
		List<Long> imageIds = List.of(10L, 20L);
		List<String> imageUrls = List.of("url1", "url2");

		Mockito.when(board.getId()).thenReturn(boardId);
		Mockito.when(imageService.createImage(imageList)).thenReturn(imageIds);
		Mockito.when(boardImageService.getBoardImageUrls(boardId)).thenReturn(imageUrls);

		// when
		List<String> result = boardService.processImages(board, imageList);

		// then
		Mockito.verify(imageService).createImage(imageList);
		Mockito.verify(boardImageService).saveBoardImages(boardId, imageIds);
		Mockito.verify(boardImageService).getBoardImageUrls(boardId);
		Assertions.assertEquals(imageUrls, result);
	}
}
*/
