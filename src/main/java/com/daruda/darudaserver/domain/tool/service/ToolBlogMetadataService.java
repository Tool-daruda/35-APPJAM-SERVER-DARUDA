package com.daruda.darudaserver.domain.tool.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.tool.dto.response.ToolBlogResponse;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolBlog;
import com.daruda.darudaserver.domain.tool.repository.ToolBlogRepository;
import com.daruda.darudaserver.global.scraper.OgMetadataScraper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 블로그 메타데이터 지연 백필 전용 빈.
 * {@code ToolService}의 조회는 {@code readOnly} 트랜잭션이므로, 여기서 쓰기를 별도 트랜잭션으로 분리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ToolBlogMetadataService {

	private final ToolBlogRepository toolBlogRepository;
	private final OgMetadataScraper ogMetadataScraper;

	/**
	 * 메타데이터가 비어 있는 블로그 행을 스크래핑해 채운 뒤 툴의 전체 블로그를 응답 DTO로 반환한다.
	 * 스크래핑 실패는 무시하고 해당 행의 메타데이터는 null로 남긴다(GET 실패 아님).
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<ToolBlogResponse> backfillAndGet(final Tool tool) {
		List<ToolBlog> blogList = toolBlogRepository.findAllByTool(tool);
		for (ToolBlog blog : blogList) {
			if (!blog.needsMetadataBackfill()) {
				continue;
			}
			// 실패해도 applyMetadata(null) 로 "시도 시각"을 남겨 다음 조회에서 재시도하지 않게 한다.
			blog.applyMetadata(ogMetadataScraper.fetch(blog.getBlogUrl()).orElse(null));
		}
		return blogList.stream()
			.map(ToolBlogResponse::from)
			.toList();
	}
}
