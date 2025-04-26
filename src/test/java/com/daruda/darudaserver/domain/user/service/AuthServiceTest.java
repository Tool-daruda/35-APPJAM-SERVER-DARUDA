package com.daruda.darudaserver.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.daruda.darudaserver.domain.comment.repository.CommentRepository;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.community.repository.BoardScrapRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolScrapRepository;
import com.daruda.darudaserver.domain.user.dto.response.JwtTokenResponse;
import com.daruda.darudaserver.domain.user.dto.response.LoginResponse;
import com.daruda.darudaserver.domain.user.dto.response.SignUpSuccessResponse;
import com.daruda.darudaserver.domain.user.dto.response.UserInfo;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.domain.user.entity.enums.SocialType;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.auth.jwt.service.TokenService;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private BoardRepository boardRepository;

	@Mock
	private BoardScrapRepository boardScrapRepository;

	@Mock
	private CommentRepository commentRepository;

	@Mock
	private ToolScrapRepository toolScrapRepository;

	@Mock
	private TokenService tokenService;

	@Mock
	private KakaoService kakaoService;

	@InjectMocks
	private AuthService authService;

	@Test
	@DisplayName("회원 가입 성공")
	void registerSuccess() {
		// given
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;
		UserEntity mockUser = UserEntity.of(email, nickname, positions);
		ReflectionTestUtils.setField(mockUser, "id", 1L);
		JwtTokenResponse mockTokenResponse = new JwtTokenResponse("accessToken", "refreshToken");

		when(userRepository.existsByEmail(email)).thenReturn(false);
		when(userRepository.save(any(UserEntity.class))).thenReturn(mockUser);
		when(tokenService.createToken(1L)).thenReturn(mockTokenResponse);

		// when
		SignUpSuccessResponse response = authService.register(email, nickname, positions);

		// then
		assertThat(response).isNotNull();
		assertThat(response.email()).isEqualTo(email);
		assertThat(response.nickname()).isEqualTo(nickname);
		assertThat(response.jwtTokenResponse()).isEqualTo(mockTokenResponse);

		verify(userRepository).existsByEmail(email);
		verify(userRepository).save(any(UserEntity.class));
		verify(tokenService).createToken(1L);
	}

	@Test
	@DisplayName("회원 가입 실패 - 중복 이메일")
	void registerFail_DuplicatedEmail() {
		// given
		String email = "test@example.com";
		String nickname = "tester";
		Positions positions = Positions.STUDENT;

		when(userRepository.existsByEmail(email)).thenReturn(true);

		// when
		BusinessException exception = assertThrows(BusinessException.class,
			() -> authService.register(email, nickname, positions));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_EMAIL);

		verify(userRepository).existsByEmail(email);
		verify(userRepository, never()).save(any(UserEntity.class));
		verify(tokenService, never()).createToken(anyLong());
	}

	@Test
	@DisplayName("로그인 성공 - 등록된 회원")
	void loginSuccess_ExistUser() {
		// given
		String email = "test@example.com";
		String nickname = "tester";
		Long userId = 1L;
		UserEntity mockUser = mock(UserEntity.class);
		JwtTokenResponse mockTokenResponse = JwtTokenResponse.of("accessToken", "refreshToken");

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
		when(mockUser.getId()).thenReturn(userId);
		when(mockUser.getNickname()).thenReturn(nickname);
		when(tokenService.createToken(userId)).thenReturn(mockTokenResponse);

		UserInfo userInfo = UserInfo.of(userId, email, nickname);

		// when
		LoginResponse response = authService.login(userInfo);

		// then
		assertThat(response).isNotNull();
		assertThat(response.email()).isEqualTo(null);
		assertThat(response.isUser()).isTrue();
		assertThat(response.nickname()).isEqualTo(nickname);
		assertThat(response.jwtTokenResponse()).isEqualTo(mockTokenResponse);

		verify(userRepository).findByEmail(email);
		verify(tokenService).createToken(userId);
	}

	@Test
	@DisplayName("로그인 성공 - 등록되지 않은 회원")
	void loginSuccess_NewUser() {
		// given
		String email = "test@example.com";
		String nickname = "tester";
		Long userId = 1L;

		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		UserInfo userInfo = UserInfo.of(userId, email, nickname);

		// when
		LoginResponse response = authService.login(userInfo);

		// then
		assertThat(response).isNotNull();
		assertThat(response.email()).isEqualTo(email);
		assertThat(response.isUser()).isFalse();
		assertThat(response.nickname()).isEqualTo(null);
		assertThat(response.jwtTokenResponse()).isEqualTo(null);

		verify(userRepository).findByEmail(email);
		verify(tokenService, never()).createToken(anyLong());
	}

	@Test
	@DisplayName("로그아웃 성공")
	void logoutSuccess() {
		// given
		Long userId = 1L;

		// when
		Long result = authService.logout(userId);

		// then
		assertThat(result).isEqualTo(userId);

		verify(tokenService).deleteRefreshToken(userId);
	}

	@Test
	@DisplayName("회원 탈퇴 성공")
	void withdrawSuccess() {
		// given
		Long userId = 1L;
		UserEntity mockUser = mock(UserEntity.class);
		List<Board> mockBoardList = List.of(mock(Board.class), mock(Board.class));

		when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
		when(boardRepository.findAllByUserId(userId)).thenReturn(mockBoardList);

		// when
		authService.withdraw(userId);

		// then
		verify(userRepository).findById(userId);
		verify(toolScrapRepository).deleteAllByUserId(userId);
		verify(commentRepository).deleteAllByUserId(userId);
		verify(boardRepository).findAllByUserId(userId);
		verify(commentRepository, times(mockBoardList.size())).deleteByBoardId(anyLong());
		verify(boardScrapRepository).deleteAllByUserId(userId);
		verify(boardRepository).deleteAllByUserId(userId);
		verify(tokenService).deleteRefreshToken(userId);
		verify(userRepository).delete(mockUser);
	}

	@Test
	@DisplayName("회원 탈퇴 실패 - 사용자 없음")
	void withdrawFail_UserNotFound() {
		// given
		Long userId = 1L;

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// when
		BusinessException exception = assertThrows(BusinessException.class,
			() -> authService.withdraw(userId));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);

		verify(userRepository).findById(userId);
		verify(toolScrapRepository, never()).deleteAllByUserId(userId);
		verify(commentRepository, never()).deleteAllByUserId(userId);
		verify(boardRepository, never()).findAllByUserId(userId);
		verify(commentRepository, never()).deleteByBoardId(anyLong());
		verify(boardScrapRepository, never()).deleteAllByUserId(userId);
		verify(boardRepository, never()).deleteAllByUserId(userId);
		verify(tokenService, never()).deleteRefreshToken(userId);
		verify(userRepository, never()).delete(any(UserEntity.class));
	}

	@Test
	@DisplayName("소셜 서비스 찾기 성공 - KAKAO")
	void findSocialService_Kakao() {
		// given
		SocialType socialType = SocialType.KAKAO;

		// when
		SocialService result = authService.findSocialService(socialType);

		// then
		assertThat(result).isEqualTo(kakaoService);
	}
}
