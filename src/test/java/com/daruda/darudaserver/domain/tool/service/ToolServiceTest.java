package com.daruda.darudaserver.domain.tool.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.daruda.darudaserver.domain.tool.dto.response.ToolBlogListResponse;
import com.daruda.darudaserver.domain.tool.dto.response.ToolBlogResponse;
import com.daruda.darudaserver.domain.tool.dto.response.ToolLikeResponse;
import com.daruda.darudaserver.domain.tool.dto.response.ToolListResponse;
import com.daruda.darudaserver.domain.tool.entity.Category;
import com.daruda.darudaserver.domain.tool.entity.License;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolBlog;
import com.daruda.darudaserver.domain.tool.entity.ToolKeyword;
import com.daruda.darudaserver.domain.tool.entity.ToolLike;
import com.daruda.darudaserver.domain.tool.repository.ToolBlogRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolKeywordRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolLikeRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolRepository;
import com.daruda.darudaserver.domain.user.entity.User;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.scraper.OgMetadata;

@ExtendWith(MockitoExtension.class)
class ToolServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private ToolRepository toolRepository;

	@Mock
	private ToolLikeRepository toolLikeRepository;

	@Mock
	private ToolLikeInternalService toolLikeInternalService;

	@Mock
	private ToolKeywordRepository toolKeywordRepository;

	@Mock
	private ToolBlogRepository toolBlogRepository;

	@Mock
	private ToolBlogMetadataService toolBlogMetadataService;

	@InjectMocks
	private ToolService toolService;

	@DisplayName("유저 조회 성공")
	@Test
	void getUserById_Success() {
		// given
		Long userId = 1L;
		User mockUser = User.builder()
			.email("test@example.com")
			.nickname("tester")
			.positions(null)
			.build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

		// when
		User result = toolService.getUserById(userId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getEmail()).isEqualTo("test@example.com");
		assertThat(result.getNickname()).isEqualTo("tester");
	}

	@Test
	void getUserById_Fail() {
		// given
		Long userId = 1L;

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> toolService.getUserById(userId))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorCode.DATA_NOT_FOUND.getMessage());
	}

	@DisplayName("좋아요가 없던 툴에 좋아요를 누르면 좋아요가 활성화된다")
	@Test
	void postToolLike_Create() {
		// given
		Long userId = 1L;
		Long toolId = 10L;
		User user = User.builder()
			.email("test@example.com")
			.nickname("tester")
			.positions(null)
			.build();
		Tool tool = Tool.builder().id(toolId).build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(toolRepository.findById(toolId)).thenReturn(Optional.of(tool));
		when(toolLikeRepository.existsByUserAndTool(user, tool)).thenReturn(false);
		when(toolLikeInternalService.saveIfAbsent(any(ToolLike.class))).thenReturn(true);
		when(toolLikeRepository.countByTool_Id(toolId)).thenReturn(1);

		// when
		ToolLikeResponse result = toolService.postToolLike(userId, toolId);

		// then
		assertThat(result.toolId()).isEqualTo(toolId);
		assertThat(result.liked()).isTrue();
		assertThat(result.likeCount()).isEqualTo(1);
		verify(toolLikeInternalService).saveIfAbsent(any(ToolLike.class));
	}

	@DisplayName("이미 좋아요한 툴에 다시 좋아요를 누르면 좋아요가 취소된다")
	@Test
	void postToolLike_Toggle_Off() {
		// given
		Long userId = 1L;
		Long toolId = 10L;
		User user = User.builder()
			.email("test@example.com")
			.nickname("tester")
			.positions(null)
			.build();
		Tool tool = Tool.builder().id(toolId).build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(toolRepository.findById(toolId)).thenReturn(Optional.of(tool));
		when(toolLikeRepository.existsByUserAndTool(user, tool)).thenReturn(true);
		when(toolLikeRepository.countByTool_Id(toolId)).thenReturn(0);

		// when
		ToolLikeResponse result = toolService.postToolLike(userId, toolId);

		// then
		assertThat(result.liked()).isFalse();
		assertThat(result.likeCount()).isEqualTo(0);
		verify(toolLikeRepository).deleteByUserAndTool(user, tool);
		verify(toolLikeInternalService, never()).saveIfAbsent(any(ToolLike.class));
	}

	@DisplayName("동시성 문제로 좋아요 중복 삽입 시 false를 반환한다")
	@Test
	void postToolLike_ConcurrentInsert() {
		// given
		Long userId = 1L;
		Long toolId = 10L;
		User user = User.builder()
			.email("test@example.com")
			.nickname("tester")
			.positions(null)
			.build();
		Tool tool = Tool.builder().id(toolId).build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(toolRepository.findById(toolId)).thenReturn(Optional.of(tool));
		when(toolLikeRepository.existsByUserAndTool(user, tool)).thenReturn(false);
		when(toolLikeInternalService.saveIfAbsent(any(ToolLike.class))).thenReturn(false);
		when(toolLikeRepository.countByTool_Id(toolId)).thenReturn(1);

		// when
		ToolLikeResponse result = toolService.postToolLike(userId, toolId);

		// then
		assertThat(result.liked()).isFalse();
		verify(toolLikeInternalService).saveIfAbsent(any(ToolLike.class));
	}

	@Nested
	@DisplayName("툴 목록 조회")
	class GetToolList {

		private Tool tool(final Long toolId, final String createdAt, final License license) {
			return Tool.builder()
				.id(toolId)
				.toolMainName("tool" + toolId)
				.toolLogo("logo" + toolId)
				.description("description" + toolId)
				.license(license)
				.category(Category.AI)
				.createdAt(Timestamp.valueOf(createdAt))
				.build();
		}

		@DisplayName("키워드가 없는 툴이 페이지에 포함돼도 예외 없이 전체 페이지를 반환한다")
		@Test
		void getToolList_toolWithoutKeywords_doesNotThrow() {
			// given
			Tool toolWithKeywords = tool(1L, "2024-01-03 00:00:00", License.FREE);
			Tool toolWithoutKeywords = tool(2L, "2024-01-02 00:00:00", License.PAID);
			Tool anotherToolWithKeywords = tool(3L, "2024-01-01 00:00:00", License.FREE);

			given(toolRepository.findAll())
				.willReturn(List.of(toolWithKeywords, toolWithoutKeywords, anotherToolWithKeywords));
			given(toolKeywordRepository.findByTool_IdIn(anyList()))
				.willReturn(List.of(
					ToolKeyword.builder().keywordName("문서").tool(toolWithKeywords).build(),
					ToolKeyword.builder().keywordName("협업").tool(anotherToolWithKeywords).build()
				));

			// when
			ToolListResponse result = toolService.getToolList(null, "createdAt", "ALL", 18, null, false);

			// then
			assertThat(result.tools()).hasSize(3);
			assertThat(result.tools())
				.filteredOn(toolResponse -> toolResponse.toolId().equals(2L))
				.singleElement()
				.satisfies(toolResponse -> assertThat(toolResponse.keywords()).isEmpty());
			assertThat(result.tools())
				.filteredOn(toolResponse -> toolResponse.toolId().equals(1L))
				.singleElement()
				.satisfies(toolResponse -> assertThat(toolResponse.keywords()).containsExactly("문서"));
		}
	}

	@Nested
	@DisplayName("툴 블로그 조회")
	class GetBlog {

		@DisplayName("블로그가 하나도 없으면 NotFoundException 없이 빈 목록을 반환한다")
		@Test
		void getBlog_toolWithoutBlogs_returnsEmptyList() {
			// given
			Long toolId = 10L;
			Tool tool = Tool.builder().id(toolId).build();
			given(toolRepository.findById(toolId)).willReturn(Optional.of(tool));
			given(toolBlogRepository.findAllByTool(tool)).willReturn(List.of());

			// when
			ToolBlogListResponse result = toolService.getBlog(toolId);

			// then
			assertThat(result.toolBlogs()).isEmpty();
			then(toolBlogMetadataService).shouldHaveNoInteractions();
		}

		@DisplayName("이미 스크래핑을 시도한 블로그는 백필 빈을 호출하지 않고 필드를 그대로 반환한다")
		@Test
		void getBlog_blogAlreadyAttempted_returnedWithoutBackfill() {
			// given
			Long toolId = 10L;
			Tool tool = Tool.builder().id(toolId).build();
			OgMetadata metadata = new OgMetadata(
				"제목", "https://cdn.example.com/thumb.png", "요약", "예시 블로그", "https://example.com/favicon.ico");
			// create(...) 는 metadataFetchedAt 을 찍으므로 needsMetadataBackfill() == false
			ToolBlog blog = ToolBlog.create("https://blog.example.com/1", tool, metadata);
			ReflectionTestUtils.setField(blog, "id", 1L);
			assertThat(blog.needsMetadataBackfill()).isFalse();
			given(toolRepository.findById(toolId)).willReturn(Optional.of(tool));
			given(toolBlogRepository.findAllByTool(tool)).willReturn(List.of(blog));

			// when
			ToolBlogListResponse result = toolService.getBlog(toolId);

			// then
			assertThat(result.toolBlogs()).hasSize(1);
			assertThat(result.toolBlogs().get(0).blogId()).isEqualTo(1L);
			assertThat(result.toolBlogs().get(0).title()).isEqualTo("제목");
			assertThat(result.toolBlogs().get(0).siteName()).isEqualTo("예시 블로그");
			assertThat(result.toolBlogs().get(0).thumbnailUrl()).isEqualTo("https://cdn.example.com/thumb.png");
			then(toolBlogMetadataService).shouldHaveNoInteractions();
		}

		@DisplayName("스크래핑을 한 번도 시도하지 않은 블로그가 있으면 백필 빈에 위임한다")
		@Test
		void getBlog_blogNeverAttempted_delegatesToBackfill() {
			// given
			Long toolId = 10L;
			Tool tool = Tool.builder().id(toolId).build();
			ToolBlog blog = ToolBlog.builder()
				.blogUrl("https://blog.example.com/1")
				.tool(tool)
				.build();
			assertThat(blog.needsMetadataBackfill()).isTrue();
			ToolBlogResponse backfilled = ToolBlogResponse.builder()
				.blogId(1L)
				.blogUrl("https://blog.example.com/1")
				.title("긁어온 제목")
				.build();
			given(toolRepository.findById(toolId)).willReturn(Optional.of(tool));
			given(toolBlogRepository.findAllByTool(tool)).willReturn(List.of(blog));
			given(toolBlogMetadataService.backfillAndGet(tool)).willReturn(List.of(backfilled));

			// when
			ToolBlogListResponse result = toolService.getBlog(toolId);

			// then
			assertThat(result.toolBlogs()).containsExactly(backfilled);
			then(toolBlogMetadataService).should().backfillAndGet(tool);
		}
	}
}
