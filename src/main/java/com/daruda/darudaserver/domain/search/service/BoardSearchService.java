package com.daruda.darudaserver.domain.search.service;

import java.util.List;

import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.search.document.BoardDocument;
import com.daruda.darudaserver.domain.search.dto.response.BoardSearchResponse;
import com.daruda.darudaserver.domain.search.repository.BoardSearchRepository;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardSearchService {
	private final BoardSearchRepository boardSearchRepository;

	private final ElasticsearchTemplate elasticsearchTemplate;

	public List<BoardSearchResponse> searchByTitleAndContentAndTool(String keyword) {

		MatchQuery titleMatch = MatchQuery.of(title -> title
			.field("title")
			.query(keyword)
			.fuzziness("AUTO")
		);
		MatchQuery contentMatch = MatchQuery.of(content -> content
			.field("content")
			.query(keyword)
			.fuzziness("AUTO")
		);
		MatchQuery toolMatch = MatchQuery.of(tool -> tool
			.field("tool")
			.query(keyword)
			.fuzziness("AUTO")
		);

		Query boolQuery = BoolQuery.of(board -> board
			.should(titleMatch._toQuery())
			.should(contentMatch._toQuery())
			.should(toolMatch._toQuery())
			.minimumShouldMatch("2")
		)._toQuery();

		NativeQuery query = NativeQuery.builder()
			.withQuery(boolQuery)
			.build();

		SearchHits<BoardDocument> hits = elasticsearchTemplate.search(query, BoardDocument.class);

		return hits.getSearchHits().stream()
			.map(hit -> BoardSearchResponse.from(hit.getContent()))
			.toList();
	}
}
