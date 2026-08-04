package com.daruda.darudaserver.domain.search.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.community.service.BoardScrapService;
import com.daruda.darudaserver.domain.search.document.BoardDocument;
import com.daruda.darudaserver.domain.search.dto.response.BoardSearchResponse;
import com.daruda.darudaserver.domain.search.dto.response.GetBoardDocumentResponse;
import com.daruda.darudaserver.domain.search.repository.BoardSearchRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolScrapRepository;
import com.daruda.darudaserver.domain.tool.service.ToolService;
import com.daruda.darudaserver.domain.user.service.UserService;
import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhraseQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardSearchService {

	private final ElasticsearchTemplate elasticsearchTemplate;
	private final BoardScrapService boardScrapService;


	public GetBoardDocumentResponse searchByTitleAndContentAndTool(String keyword, String nextCursor, int size) {

		MatchQuery titleMatch = MatchQuery.of(title -> title
			.field("title")
			.query(keyword)
			.minimumShouldMatch("40%")
			.fuzziness("AUTO")
			.maxExpansions(10)
		);

		MatchPhraseQuery contentMatch = MatchPhraseQuery.of(content -> content
			.field("content")
			.query(keyword)
		);

		MatchQuery toolMainMatch = MatchQuery.of(tool -> tool
			.field("toolMainName")
			.query(keyword)
			.minimumShouldMatch("40%")
			.fuzziness("AUTO")
			.maxExpansions(10)
		);

		MatchQuery toolSubMatch = MatchQuery.of(tool -> tool
				.field("toolSubName")
				.query(keyword)
				.minimumShouldMatch("40%")
				.fuzziness("AUTO")
				.maxExpansions(10)
		);

		// 기본 boolQuery
		Query boolQuery = BoolQuery.of(bool -> bool
			.should(titleMatch._toQuery())
			.should(contentMatch._toQuery())
			.should(toolMainMatch._toQuery())
			.should(toolSubMatch._toQuery())
			.minimumShouldMatch("1")
		)._toQuery();

		// nextCursor 기반 range query 추가
		Query finalQuery;
		if (nextCursor != null && !nextCursor.equals("-1")) {
			Query rangeQuery = RangeQuery.of(r -> r
				.field("updatedAt")
				.lt(JsonData.of(nextCursor))
			)._toQuery();

			finalQuery = BoolQuery.of(b -> b
				.must(boolQuery)
				.must(rangeQuery)
			)._toQuery();
		} else {
			finalQuery = boolQuery;
		}

		NativeQuery query = NativeQuery.builder()
			.withQuery(finalQuery)
			.withPageable(PageRequest.of(0, size + 1))  // size + 1로 hasNext 확인
			.withSort(Sort.by(
				Sort.Order.desc("updatedAt"),
				Sort.Order.desc("id")
			))
			.build();

		SearchHits<BoardDocument> hits = elasticsearchTemplate.search(query, BoardDocument.class);

		List<BoardDocument> documents = hits.getSearchHits().stream()
			.map(hit -> hit.getContent())
			.toList();

		// 스크랩 수는 색인(ES)에 두면 스크랩 변동마다 재색인이 필요해 최신성이 떨어지므로, DB에서 일괄 조회해 채운다.
		List<Long> boardIds = documents.stream()
			.map(doc -> Long.valueOf(doc.getId()))
			.toList();
		Map<Long, Long> scrapCountMap = boardScrapService.getScrapCountMap(boardIds);

		List<BoardSearchResponse> boardSearchResponses = documents.stream()
			.map(doc -> BoardSearchResponse.from(doc,
				scrapCountMap.getOrDefault(Long.valueOf(doc.getId()), 0L)))
			.toList();

		boolean hasNext = boardSearchResponses.size() > size;
		List<BoardSearchResponse> paginatedResponses = hasNext
			? boardSearchResponses.subList(0, size)
			: boardSearchResponses;

		long newNextCursor = hasNext
			? paginatedResponses.get(paginatedResponses.size() - 1).updatedAt().getTime()
			: -1L;

		return GetBoardDocumentResponse.of(
			paginatedResponses,
			ScrollPaginationDto.of(hits.getTotalHits(), newNextCursor)
		);
	}

}
