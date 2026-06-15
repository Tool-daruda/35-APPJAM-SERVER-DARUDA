package com.daruda.darudaserver.global.config;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
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
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchIndexInitializer implements ApplicationRunner {

	private final ElasticsearchOperations elasticsearchOperations;
	private final ToolRepository toolRepository;
	private final ToolSearchRepository toolSearchRepository;
	private final BoardRepository boardRepository;
	private final BoardSearchRepository boardSearchRepository;
	private final BoardImageService boardImageService;
	private final CommentRepository commentRepository;

	@Override
	@Transactional(readOnly = true)
	public void run(ApplicationArguments args) {
		log.info("Elasticsearch 인덱스 초기화 시작...");
		initializeIndex(BoardDocument.class);
		initializeIndex(ToolDocument.class);
		syncIfEmpty();
		log.info("Elasticsearch 인덱스 초기화 완료.");
	}

	private void initializeIndex(Class<?> clazz) {
		try {
			IndexOperations indexOps = elasticsearchOperations.indexOps(clazz);
			if (!indexOps.exists()) {
				log.info("{} 인덱스가 존재하지 않아 생성을 시도합니다.", clazz.getSimpleName());
				boolean created = indexOps.create();
				if (!created && !indexOps.exists()) {
					throw new IllegalStateException(clazz.getSimpleName() + " 인덱스 생성 실패");
				}

				if (!indexOps.putMapping(indexOps.createMapping())) {
					throw new IllegalStateException(clazz.getSimpleName() + " 인덱스 매핑 적용 실패");
				}

				log.info("{} 인덱스 생성/매핑 성공.", clazz.getSimpleName());
			} else {
				log.info("{} 인덱스가 이미 존재합니다.", clazz.getSimpleName());
			}
		} catch (Exception e) {
			log.error("{} 인덱스 초기화 실패", clazz.getSimpleName(), e);
			throw new IllegalStateException(clazz.getSimpleName() + " 인덱스 초기화 실패", e);
		}
	}

	private void syncIfEmpty() {
		long dbToolCount = toolRepository.count();
		long esToolCount = toolSearchRepository.count();
		if (dbToolCount != esToolCount) {
			log.info("Tool 불일치 감지 - DB: {}건, ES: {}건. 동기화 시작.", dbToolCount, esToolCount);
			syncTools();
		}

		long dbBoardCount = boardRepository.count();
		long esBoardCount = boardSearchRepository.count();
		if (dbBoardCount != esBoardCount) {
			log.info("Board 불일치 감지 - DB: {}건, ES: {}건. 동기화 시작.", dbBoardCount, esBoardCount);
			syncBoards();
		}
	}

	private void syncTools() {
		List<Tool> tools = toolRepository.findAll();
		if (tools.isEmpty()) {
			return;
		}
		List<ToolDocument> docs = tools.stream()
			.map(ToolDocument::from)
			.toList();
		toolSearchRepository.saveAll(docs);
		log.info("Tool ES 동기화 완료: {}건", docs.size());
	}

	private void syncBoards() {
		List<Board> boards = boardRepository.findAll();
		if (boards.isEmpty()) {
			return;
		}
		List<BoardDocument> docs = boards.stream()
			.map(board -> BoardDocument.from(
				board,
				boardImageService.getBoardImageUrls(board.getId()),
				commentRepository.countByBoardId(board.getId()),
				false
			))
			.toList();
		boardSearchRepository.saveAll(docs);
		log.info("Board ES 동기화 완료: {}건", docs.size());
	}
}
