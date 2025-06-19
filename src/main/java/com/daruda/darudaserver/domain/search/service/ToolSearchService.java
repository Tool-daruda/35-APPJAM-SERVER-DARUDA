package com.daruda.darudaserver.domain.search.service;

import java.util.List;

import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.daruda.darudaserver.domain.search.document.ToolDocument;
import com.daruda.darudaserver.domain.search.dto.response.ToolSearchResponse;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ToolSearchService {

	private final ElasticsearchTemplate elasticsearchTemplate;

	public List<ToolSearchResponse> searchByName(String keyword) {
		// toolMainName 또는 toolSubName에 하나라도 포함되면 검색되도록 OR 조건 구성
		MatchQuery mainMatch = MatchQuery.of(m -> m
			.field("toolMainName")
			.query(keyword)
			.fuzziness("AUTO")
		);

		MatchQuery subMatch = MatchQuery.of(m -> m
			.field("toolSubName")
			.query(keyword)
			.fuzziness("AUTO")
		);

		BoolQuery boolQuery = BoolQuery.of(b -> b
			.should(mainMatch._toQuery())
			.should(subMatch._toQuery())
		);

		NativeQuery query = NativeQuery.builder()
			.withQuery(boolQuery._toQuery())
			.build();

		SearchHits<ToolDocument> hits = elasticsearchTemplate.search(query, ToolDocument.class);

		return hits.getSearchHits().stream()
			.map(hit -> ToolSearchResponse.from(hit.getContent()))
			.toList();
	}
}
