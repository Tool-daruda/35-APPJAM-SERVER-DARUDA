package com.daruda.darudaserver.domain.search;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.community.service.BoardImageService;
import com.daruda.darudaserver.domain.search.document.BoardDocument;
import com.daruda.darudaserver.domain.search.document.ToolDocument;
import com.daruda.darudaserver.domain.search.repository.BoardSearchRepository;
import com.daruda.darudaserver.domain.search.repository.ToolSearchRepository;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.repository.ToolRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ElasticsearchDataLoader implements CommandLineRunner {
	private final BoardRepository boardRepository;
	private final BoardSearchRepository boardSearchRepository;
	private final BoardImageService boardImageService;

	private final ToolRepository toolRepository;
	private final ToolSearchRepository toolSearchRepository;

	@Override
	@Transactional
	public void run(String... args) {
		List<Board> boards = boardRepository.findAll();
		List<BoardDocument> documents = boards.stream()
			.map(board -> BoardDocument.from(board, boardImageService.getBoardImageUrls(board.getId())))
			.toList();

		boardSearchRepository.saveAll(documents);
		System.out.println("✅ Elasticsearch 인덱싱 완료: " + documents.size() + "건");

		List<Tool> tools = toolRepository.findAll();

		List<ToolDocument> toolDocuments = tools.stream()
			.map(ToolDocument::from)
			.toList();

		toolSearchRepository.saveAll(toolDocuments);
		System.out.println("✅ Elasticsearch 인덱싱 완료: " + documents.size() + "건");
	}

}
