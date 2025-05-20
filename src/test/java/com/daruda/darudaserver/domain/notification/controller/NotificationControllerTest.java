package com.daruda.darudaserver.domain.notification.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.daruda.darudaserver.domain.comment.entity.CommentEntity;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.notification.dto.request.NoticeRequest;
import com.daruda.darudaserver.domain.notification.dto.response.NotificationResponse;
import com.daruda.darudaserver.domain.notification.entity.NotificationEntity;
import com.daruda.darudaserver.domain.notification.entity.enums.NotificationType;
import com.daruda.darudaserver.domain.notification.service.NotificationService;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.global.auth.jwt.provider.JwtTokenProvider;
import com.daruda.darudaserver.global.auth.security.JwtAuthenticationFilter;
import com.daruda.darudaserver.global.auth.security.UserAuthentication;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

	@Mock
	private NotificationService notificationService;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private NotificationController notificationController;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
			.setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
			.addFilters(new JwtAuthenticationFilter(jwtTokenProvider))
			.build();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("SSE 연결 성공")
	void subscribe_ShouldReturnSseEmitter() throws Exception {
		// given
		Long userId = 1L;
		Authentication authentication = UserAuthentication.createUserAuthentication(userId);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);

		// when
		when(jwtTokenProvider.generateAccessToken(authentication)).thenReturn("accessToken");

		// then
		String token = jwtTokenProvider.generateAccessToken(authentication);

		mockMvc.perform(get("/api/v1/notification/connect")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("알림 읽음 처리 성공")
	void readNotification_ShouldMarkAsRead() throws Exception {
		// given
		Long userId = 1L;
		Long notificationId = 1L;
		Authentication authentication = UserAuthentication.createUserAuthentication(userId);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);

		// when
		when(jwtTokenProvider.generateAccessToken(authentication)).thenReturn("accessToken");

		// then
		String token = jwtTokenProvider.generateAccessToken(authentication);

		mockMvc.perform(patch("/api/v1/notification/read/{notification-id}", notificationId)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.statusCode").value(SuccessCode.SUCCESS_UPDATE.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.SUCCESS_UPDATE.getMessage()));
	}

	@Test
	@DisplayName("전체 알림 목록 조회 성공")
	void getNotifications_ShouldReturnNotificationList() throws Exception {
		// given
		Long userId = 1L;
		Authentication authentication = UserAuthentication.createUserAuthentication(userId);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		UserEntity userEntity = UserEntity.of(email, nickname, positions);
		ReflectionTestUtils.setField(userEntity, "id", userId);

		String toolName = "test";
		Tool tool = Tool.builder().toolMainName(toolName).build();

		String title = "title";
		String content = "content";
		Board board = Board.create(tool, userEntity, title, content);

		String photoUrl = "http://test.test";
		CommentEntity comment = CommentEntity.of(content, photoUrl, userEntity, board);

		NotificationEntity notification = NotificationEntity.of(userEntity, NotificationType.COMMENT, title, content,
			comment);
		List<NotificationResponse> notifications = List.of(NotificationResponse.from(notification));

		// when
		when(notificationService.getNotifications(anyLong())).thenReturn(notifications);
		when(jwtTokenProvider.generateAccessToken(authentication)).thenReturn("accessToken");

		// then
		String token = jwtTokenProvider.generateAccessToken(authentication);

		mockMvc.perform(get("/api/v1/notification")
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.statusCode").value(SuccessCode.SUCCESS_FETCH.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.SUCCESS_FETCH.getMessage()));
	}

	@Test
	@DisplayName("최근 알림 목록 조회 성공")
	void getRecentNotifications_ShouldReturnRecentNotificationList() throws Exception {
		// given
		Long userId = 1L;
		Authentication authentication = UserAuthentication.createUserAuthentication(userId);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		UserEntity userEntity = UserEntity.of(email, nickname, positions);
		ReflectionTestUtils.setField(userEntity, "id", userId);

		String toolName = "test";
		Tool tool = Tool.builder().toolMainName(toolName).build();

		String title = "title";
		String content = "content";
		Board board = Board.create(tool, userEntity, title, content);

		String photoUrl = "http://test.test";
		CommentEntity comment = CommentEntity.of(content, photoUrl, userEntity, board);

		NotificationEntity notification = NotificationEntity.of(userEntity, NotificationType.COMMENT, title, content,
			comment);
		List<NotificationResponse> notifications = List.of(NotificationResponse.from(notification));

		// when
		when(notificationService.getRecentNotifications(anyLong())).thenReturn(notifications);
		when(jwtTokenProvider.generateAccessToken(authentication)).thenReturn("accessToken");

		// then
		String token = jwtTokenProvider.generateAccessToken(authentication);

		mockMvc.perform(get("/api/v1/notification/recent")
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.statusCode").value(SuccessCode.SUCCESS_FETCH.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.SUCCESS_FETCH.getMessage()));
	}

	@Test
	@DisplayName("공지 발송 성공")
	void notice_ShouldSendNotice() throws Exception {
		// given
		Long userId = 1L;
		Authentication authentication = UserAuthentication.createUserAuthentication(userId);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		NoticeRequest request = new NoticeRequest("공지 제목", "공지 내용");

		// when
		when(jwtTokenProvider.generateAccessToken(authentication)).thenReturn("accessToken");

		// then
		String token = jwtTokenProvider.generateAccessToken(authentication);

		mockMvc.perform(post("/api/v1/notification/notice")
				.contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request))
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.statusCode").value(SuccessCode.SUCCESS_SEND_NOTICE.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.SUCCESS_SEND_NOTICE.getMessage()));
	}
}
