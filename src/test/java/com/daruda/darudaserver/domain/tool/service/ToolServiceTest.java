package com.daruda.darudaserver.domain.tool.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daruda.darudaserver.domain.tool.dto.response.ToolLikeResponse;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolLike;
import com.daruda.darudaserver.domain.tool.repository.ToolLikeRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolRepository;
import com.daruda.darudaserver.domain.user.entity.User;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;

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
		Tool tool = Tool.builder().toolId(toolId).build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(toolRepository.findById(toolId)).thenReturn(Optional.of(tool));
		when(toolLikeRepository.existsByUserAndTool(user, tool)).thenReturn(false);
		when(toolLikeInternalService.saveIfAbsent(any(ToolLike.class))).thenReturn(true);
		when(toolLikeRepository.countByTool_ToolId(toolId)).thenReturn(1);

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
		Tool tool = Tool.builder().toolId(toolId).build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(toolRepository.findById(toolId)).thenReturn(Optional.of(tool));
		when(toolLikeRepository.existsByUserAndTool(user, tool)).thenReturn(true);
		when(toolLikeRepository.countByTool_ToolId(toolId)).thenReturn(0);

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
		Tool tool = Tool.builder().toolId(toolId).build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(toolRepository.findById(toolId)).thenReturn(Optional.of(tool));
		when(toolLikeRepository.existsByUserAndTool(user, tool)).thenReturn(false);
		when(toolLikeInternalService.saveIfAbsent(any(ToolLike.class))).thenReturn(false);
		when(toolLikeRepository.countByTool_ToolId(toolId)).thenReturn(1);

		// when
		ToolLikeResponse result = toolService.postToolLike(userId, toolId);

		// then
		assertThat(result.liked()).isFalse();
		verify(toolLikeInternalService).saveIfAbsent(any(ToolLike.class));
	}
}
