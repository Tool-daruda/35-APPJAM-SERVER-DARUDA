package com.daruda.darudaserver.domain.user.controller;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.daruda.darudaserver.domain.community.service.BoardService;
import com.daruda.darudaserver.domain.user.dto.request.UpdateMyRequest;
import com.daruda.darudaserver.domain.user.dto.response.FavoriteToolsResponse;
import com.daruda.darudaserver.domain.user.dto.response.MyProfileResponse;
import com.daruda.darudaserver.domain.user.dto.response.UpdateMyResponse;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.domain.user.service.UserService;
import com.daruda.darudaserver.global.auth.jwt.provider.JwtTokenProvider;
import com.daruda.darudaserver.global.auth.security.JwtAuthenticationFilter;
import com.daruda.darudaserver.global.auth.security.UserAuthentication;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

	@Mock
	private UserService userService;

	@Mock
	private BoardService boardService;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private UserController userController;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(userController)
			.setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
			.addFilters(new JwtAuthenticationFilter(jwtTokenProvider))
			.build();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("프로필 수정 성공")
	void updateProfile_success() throws Exception {
		// given
		Long userId = 1L;
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		String positionEngName = positions.getEngName();
		Authentication authentication = UserAuthentication.createUserAuthentication(userId, positionEngName);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		UpdateMyRequest request = UpdateMyRequest.of(nickname, positions.getName());
		UpdateMyResponse response = UpdateMyResponse.of(nickname, positions);

		// when
		when(userService.updateProfile(userId, request.nickname(), request.positions())).thenReturn(response);

		// then
		String token = "accessToken";

		mockMvc.perform(patch("/api/v1/user/profile")
				.contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request))
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.nickname").value(nickname))
			.andExpect(jsonPath("$.data.positions").value("STUDENT"))
			.andExpect(jsonPath("$.statusCode").value(SuccessCode.SUCCESS_UPDATE.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.SUCCESS_UPDATE.getMessage()));
	}

	@Test
	@DisplayName("찜한 툴 목록 조회 성공")
	void getFavoriteTools_success() throws Exception {
		// given
		Long userId = 1L;
		String positionEngName = Positions.STUDENT.getEngName();
		Authentication authentication = UserAuthentication.createUserAuthentication(userId, positionEngName);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		FavoriteToolsResponse response = FavoriteToolsResponse.of(List.of());

		// when
		when(userService.getFavoriteTools(userId)).thenReturn(response);

		// then
		String token = "accessToken";

		mockMvc.perform(get("/api/v1/user/scrap-tools")
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.toolList").isArray())
			.andExpect(jsonPath("$.statusCode").value(SuccessCode.SUCCESS_FETCH.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.SUCCESS_FETCH.getMessage()));
	}

	@Test
	@DisplayName("작성한 게시글 목록 조회 성공")
	void getUserBoards_success() throws Exception {
		// given
		Long userId = 1L;
		String positionEngName = Positions.STUDENT.getEngName();
		Authentication authentication = UserAuthentication.createUserAuthentication(userId, positionEngName);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));

		// when
		when(boardService.getUserBoards(userId, pageable)).thenReturn(null);

		// hen
		String token = "accessToken";

		mockMvc.perform(get("/api/v1/user/boards")
				.header("Authorization", "Bearer " + token)
				.param("page", "1") // 페이지 번호
				.param("size", "5") // 페이지 크기
				.param("criteria", "createdAt")) // 정렬 기준
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.statusCode").value(SuccessCode.SUCCESS_FETCH.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.SUCCESS_FETCH.getMessage()));
	}

	@Test
	@DisplayName("프로필 조회 성공")
	void getMyProfile_success() throws Exception {
		// given
		Long userId = 1L;
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		String positionEngName = positions.getEngName();

		UserEntity user = UserEntity.builder()
			.nickname(nickname)
			.positions(positions)
			.build();

		ReflectionTestUtils.setField(user, "id", userId);
		ReflectionTestUtils.setField(user, "positions", positions);

		Authentication authentication = UserAuthentication.createUserAuthentication(userId, positionEngName);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		MyProfileResponse response = MyProfileResponse.of(user);

		// when
		when(userService.getMyProfile(userId)).thenReturn(response);

		// then
		String token = "accessToken";

		mockMvc.perform(get("/api/v1/user/profile")
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.userId").value(userId))
			.andExpect(jsonPath("$.data.nickname").value(nickname))
			.andExpect(jsonPath("$.data.positions").value("STUDENT"))
			.andExpect(jsonPath("$.statusCode").value(SuccessCode.SUCCESS_FETCH.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.SUCCESS_FETCH.getMessage()));
	}

	@Test
	@DisplayName("닉네임 중복 조회 성공")
	void checkDuplicate_success() throws Exception {
		// given
		Long userId = 1L;
		String nickname = "tester";
		String positionEngName = Positions.STUDENT.getEngName();
		Authentication authentication = UserAuthentication.createUserAuthentication(userId, positionEngName);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);

		// when
		when(userService.isDuplicatedNickname(nickname)).thenReturn(false);

		// then
		String token = "accessToken";

		mockMvc.perform(
				get("/api/v1/user/nickname")
					.header("Authorization", "Bearer " + token)
					.param("nickname", nickname))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").value(false))
			.andExpect(jsonPath("$.statusCode").value(SuccessCode.SUCCESS_FETCH.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.SUCCESS_FETCH.getMessage()));
	}

	@Test
	@DisplayName("스크랩 글 목록 조회 성공")
	void getFavoriteBoards_success() throws Exception {
		// given
		Long userId = 1L;
		String positionEngName = Positions.STUDENT.getEngName();
		Authentication authentication = UserAuthentication.createUserAuthentication(userId, positionEngName);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);

		// then
		String token = "accessToken";

		mockMvc.perform(get("/api/v1/user/scrap-boards")
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.statusCode").value(SuccessCode.SUCCESS_FETCH.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.SUCCESS_FETCH.getMessage()));
	}
}
