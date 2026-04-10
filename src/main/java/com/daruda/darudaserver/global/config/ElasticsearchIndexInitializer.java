package com.daruda.darudaserver.global.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

import com.daruda.darudaserver.domain.search.document.BoardDocument;
import com.daruda.darudaserver.domain.search.document.ToolDocument;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchIndexInitializer {

	private final ElasticsearchOperations elasticsearchOperations;

	@EventListener(ApplicationReadyEvent.class)
	public void initializeIndices() {
		log.info("Elasticsearch 인덱스 초기화 시작...");
		initializeIndex(BoardDocument.class);
		initializeIndex(ToolDocument.class);
		log.info("Elasticsearch 인덱스 초기화 완료.");
	}

	private void initializeIndex(Class<?> clazz) {
		IndexOperations indexOps = elasticsearchOperations.indexOps(clazz);
		if (!indexOps.exists()) {
			log.info("{} 인덱스가 존재하지 않아 생성을 시도합니다.", clazz.getSimpleName());
			try {
				indexOps.create();
				indexOps.putMapping(indexOps.createMapping());
				log.info("{} 인덱스 생성 성공.", clazz.getSimpleName());
			} catch (Exception e) {
				log.error("{} 인덱스 생성 중 오류 발생: {}", clazz.getSimpleName(), e.getMessage());
			}
		} else {
			log.info("{} 인덱스가 이미 존재합니다.", clazz.getSimpleName());
		}
	}
}
