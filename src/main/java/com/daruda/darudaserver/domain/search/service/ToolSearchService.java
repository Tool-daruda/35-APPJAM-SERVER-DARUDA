package com.daruda.darudaserver.domain.search.service;

import java.util.List;

import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.daruda.darudaserver.domain.search.document.ToolDocument;
import com.daruda.darudaserver.domain.search.dto.response.ToolSearchResponse;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.repository.ToolRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolScrapRepository;
import com.daruda.darudaserver.domain.tool.service.ToolService;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ToolSearchService {

	private final ElasticsearchTemplate elasticsearchTemplate;
	private final ToolService toolService;
	private final ToolScrapRepository toolScrapRepository;
	private final ToolRepository toolRepository;

	public List<ToolSearchResponse> searchByName(String keyword, Long userId) {
		log.debug("userId={}", userId);
		// toolMainName 또는 toolSubName에 하나라도 포함되면 검색되도록 OR 조건 구성
		MatchQuery mainMatch = MatchQuery.of(m -> m
			.field("toolMainName")
			.query(keyword)
			.minimumShouldMatch("30%")
			.fuzziness("AUTO")
		);

		MatchQuery subMatch = MatchQuery.of(m -> m
			.field("toolSubName")
			.query(keyword)
			.minimumShouldMatch("30%")
			.fuzziness("AUTO")
		);

		BoolQuery boolQuery = BoolQuery.of(b -> b
			.should(mainMatch._toQuery())
			.should(subMatch._toQuery())
			.minimumShouldMatch("1")
		);

		NativeQuery query = NativeQuery.builder()
			.withQuery(boolQuery._toQuery())
			.build();

		SearchHits<ToolDocument> hits = elasticsearchTemplate.search(query, ToolDocument.class);

		return hits.getSearchHits().stream()
			.map(hit -> {
				ToolDocument doc = hit.getContent();
				List<String> keywordList = toolService.getKeywords(Long.valueOf(doc.getId()));
				log.debug("keywordList={}", keywordList);
				boolean isScrapped = checkIfScraped(userId, Long.valueOf(doc.getId()));
				return ToolSearchResponse.from(doc, keywordList, isScrapped);
			})
			.toList();
	}

	public boolean checkIfScraped(Long userId, Long toolId) {
		log.debug("userId={}", userId);
		if (userId == null) {
			return false;
		}

		Tool tool = toolRepository.findById(toolId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.TOOL_NOT_FOUND));

		return toolScrapRepository.existsByUserAndTool(toolService.getUserById(userId), tool);

	}
}
