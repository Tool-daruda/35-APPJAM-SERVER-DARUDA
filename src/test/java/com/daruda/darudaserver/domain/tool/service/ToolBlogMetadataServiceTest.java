package com.daruda.darudaserver.domain.tool.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daruda.darudaserver.domain.tool.dto.response.ToolBlogResponse;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolBlog;
import com.daruda.darudaserver.domain.tool.repository.ToolBlogRepository;
import com.daruda.darudaserver.global.scraper.OgMetadata;
import com.daruda.darudaserver.global.scraper.OgMetadataScraper;

@ExtendWith(MockitoExtension.class)
class ToolBlogMetadataServiceTest {

	@Mock
	private ToolBlogRepository toolBlogRepository;

	@Mock
	private OgMetadataScraper ogMetadataScraper;

	@InjectMocks
	private ToolBlogMetadataService toolBlogMetadataService;

	private final Tool tool = Tool.builder().id(10L).build();

	private ToolBlog blogNeedingBackfill(final String url) {
		return ToolBlog.builder()
			.blogUrl(url)
			.tool(tool)
			.build();
	}

	@DisplayName("스크래핑에 성공하면 메타데이터를 채우고 재시도 대상에서 제외한다")
	@Test
	void backfillAndGet_scrapeSucceeds_appliesMetadata() {
		// given
		ToolBlog blog = blogNeedingBackfill("https://blog.example.com/1");
		OgMetadata metadata = new OgMetadata("제목", "https://cdn/thumb.png", "요약", "예시 블로그", "https://ex/favicon.ico");
		given(toolBlogRepository.findAllByTool(tool)).willReturn(List.of(blog));
		given(ogMetadataScraper.fetch("https://blog.example.com/1")).willReturn(Optional.of(metadata));

		// when
		List<ToolBlogResponse> result = toolBlogMetadataService.backfillAndGet(tool);

		// then
		assertThat(result).singleElement()
			.satisfies(response -> {
				assertThat(response.title()).isEqualTo("제목");
				assertThat(response.siteName()).isEqualTo("예시 블로그");
			});
		assertThat(blog.needsMetadataBackfill()).isFalse();
	}

	@DisplayName("스크래핑에 실패해도 시도 시각을 남겨 다음 조회에서 재시도하지 않는다")
	@Test
	void backfillAndGet_scrapeFails_stillMarksAttempted() {
		// given
		ToolBlog blog = blogNeedingBackfill("https://dead.example.com/1");
		given(toolBlogRepository.findAllByTool(tool)).willReturn(List.of(blog));
		given(ogMetadataScraper.fetch("https://dead.example.com/1")).willReturn(Optional.empty());

		// when
		List<ToolBlogResponse> result = toolBlogMetadataService.backfillAndGet(tool);

		// then
		assertThat(result).singleElement()
			.satisfies(response -> assertThat(response.title()).isNull());
		assertThat(blog.needsMetadataBackfill()).isFalse();
	}

	@DisplayName("이미 스크래핑을 시도한 블로그는 다시 긁지 않는다")
	@Test
	void backfillAndGet_alreadyAttempted_doesNotScrapeAgain() {
		// given
		ToolBlog attempted = ToolBlog.create("https://blog.example.com/1", tool, null);
		given(toolBlogRepository.findAllByTool(tool)).willReturn(List.of(attempted));

		// when
		List<ToolBlogResponse> result = toolBlogMetadataService.backfillAndGet(tool);

		// then
		assertThat(result).hasSize(1);
		then(ogMetadataScraper).shouldHaveNoInteractions();
	}
}
