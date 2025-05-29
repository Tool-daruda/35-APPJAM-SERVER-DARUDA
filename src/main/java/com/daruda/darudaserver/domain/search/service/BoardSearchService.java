package com.daruda.darudaserver.domain.search.service;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.search.document.BoardDocument;
import com.daruda.darudaserver.global.image.entity.Image;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardSearchService {
	private final ElasticsearchOperations elasticsearchOperations;

	public void save(Board board, Image image) {
		elasticsearchOperations.save(BoardDocument.from(board, image));
	}
}
