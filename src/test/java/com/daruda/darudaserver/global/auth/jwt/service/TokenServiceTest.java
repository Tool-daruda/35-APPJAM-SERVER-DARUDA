package com.daruda.darudaserver.global.auth.jwt.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daruda.darudaserver.domain.user.dto.response.JwtTokenResponse;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.global.auth.jwt.entity.Token;
import com.daruda.darudaserver.global.auth.jwt.provider.JwtTokenProvider;
import com.daruda.darudaserver.global.auth.jwt.provider.JwtValidationType;
import com.daruda.darudaserver.global.auth.jwt.repository.TokenRepository;
import com.daruda.darudaserver.global.auth.security.UserAuthentication;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.error.exception.UnauthorizedException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

	@Mock
	private TokenRepository tokenRepository;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private TokenService tokenService;

	@Test
	@DisplayName("토큰 생성 성공")
	void createToken_success() {
		// given
		Long userId = 1L;
		String positionEngName = Positions.STUDENT.getEngName();
		UserAuthentication userAuthentication = UserAuthentication.createUserAuthentication(userId, positionEngName);
		String accessToken = "accessToken";
		String refreshToken = "refreshToken";

		when(jwtTokenProvider.generateAccessToken(userAuthentication)).thenReturn(accessToken);
		when(jwtTokenProvider.generateRefreshToken(userAuthentication)).thenReturn(refreshToken);

		// when
		JwtTokenResponse response = tokenService.createToken(userId, positionEngName);

		// then
		assertNotNull(response);
		assertEquals(accessToken, response.accessToken());
		assertEquals(refreshToken, response.refreshToken());
		verify(tokenRepository).save(any(Token.class));
	}

	@Test
	@DisplayName("토큰 재발급 성공")
	void reissueToken_success() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		String refreshToken = "validRefreshToken";
		Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
		Long userId = 1L;
		String positionEngName = Positions.STUDENT.getEngName();
		UserAuthentication userAuthentication = UserAuthentication.createUserAuthentication(userId, positionEngName);
		String newAccessToken = "newAccessToken";
		String newRefreshToken = "newRefreshToken";

		// when
		when(request.getCookies()).thenReturn(new Cookie[] {refreshCookie});
		when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(JwtValidationType.VALID_JWT);
		when(jwtTokenProvider.getUserIdFromJwt(refreshToken)).thenReturn(userId);
		when(jwtTokenProvider.getRoleFromJwt(refreshToken)).thenReturn(positionEngName);
		when(tokenRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(Token.of(userId, refreshToken)));
		when(jwtTokenProvider.generateAccessToken(userAuthentication)).thenReturn(newAccessToken);
		when(jwtTokenProvider.generateRefreshToken(userAuthentication)).thenReturn(newRefreshToken);

		// then
		JwtTokenResponse response = tokenService.reissueToken(request);

		assertNotNull(response);
		assertEquals(newAccessToken, response.accessToken());
		assertEquals(newRefreshToken, response.refreshToken());
		verify(tokenRepository).save(any(Token.class));
	}

	@Test
	@DisplayName("토큰 재발급 실패 - 만료된 토큰")
	void reissueToken_expiredToken() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		String refreshToken = "expiredRefreshToken";
		Cookie refreshCookie = new Cookie("refreshToken", refreshToken);

		// when
		when(request.getCookies()).thenReturn(new Cookie[] {refreshCookie});
		when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(JwtValidationType.EXPIRED_JWT_TOKEN);

		// then
		UnauthorizedException exception = assertThrows(UnauthorizedException.class,
			() -> tokenService.reissueToken(request));
		assertEquals(ErrorCode.REFRESH_TOKEN_EXPIRED_ERROR, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 재발급 실패 - 유효하지 않은 토큰")
	void reissueToken_invalidToken() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		String refreshToken = "invalidRefreshToken";
		Cookie refreshCookie = new Cookie("refreshToken", refreshToken);

		// when
		when(request.getCookies()).thenReturn(new Cookie[] {refreshCookie});
		when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(JwtValidationType.INVALID_JWT_TOKEN);

		// then
		BadRequestException exception = assertThrows(BadRequestException.class,
			() -> tokenService.reissueToken(request));
		assertEquals(ErrorCode.INVALID_REFRESH_TOKEN_ERROR, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 재발급 실패 - 잘못된 서명")
	void reissueToken_invalidSignature() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		String refreshToken = "invalidSignatureRefreshToken";
		Cookie refreshCookie = new Cookie("refreshToken", refreshToken);

		// when
		when(request.getCookies()).thenReturn(new Cookie[] {refreshCookie});
		when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(JwtValidationType.INVALID_JWT_SIGNATURE);

		// then
		BadRequestException exception = assertThrows(BadRequestException.class,
			() -> tokenService.reissueToken(request));
		assertEquals(ErrorCode.REFRESH_TOKEN_SIGNATURE_ERROR, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 재발급 실패 - 지원하지 않는 토큰")
	void reissueToken_unsupportedToken() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		String refreshToken = "unsupportedRefreshToken";
		Cookie refreshCookie = new Cookie("refreshToken", refreshToken);

		// when
		when(request.getCookies()).thenReturn(new Cookie[] {refreshCookie});
		when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(JwtValidationType.UNSUPPORTED_JWT_TOKEN);

		// then
		BadRequestException exception = assertThrows(BadRequestException.class,
			() -> tokenService.reissueToken(request));
		assertEquals(ErrorCode.UNSUPPORTED_REFRESH_TOKEN_ERROR, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 재발급 실패 - 비어있는 토큰")
	void reissueToken_emptyToken() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		String refreshToken = "";
		Cookie refreshCookie = new Cookie("refreshToken", refreshToken);

		// when
		when(request.getCookies()).thenReturn(new Cookie[] {refreshCookie});
		when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(JwtValidationType.EMPTY_JWT);

		// then
		BadRequestException exception = assertThrows(BadRequestException.class,
			() -> tokenService.reissueToken(request));
		assertEquals(ErrorCode.REFRESH_TOKEN_EMPTY_ERROR, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 재발급 실패 - 사용자 ID 불일치")
	void reissueToken_userIdMismatch() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		String refreshToken = "validRefreshToken";
		Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
		Long userId = 1L;
		Long storedUserId = 2L;

		// when
		when(request.getCookies()).thenReturn(new Cookie[] {refreshCookie});
		when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(JwtValidationType.VALID_JWT);
		when(jwtTokenProvider.getUserIdFromJwt(refreshToken)).thenReturn(userId);
		when(tokenRepository.findByRefreshToken(refreshToken)).thenReturn(
			Optional.of(Token.of(storedUserId, refreshToken)));

		// then
		BusinessException exception = assertThrows(BadRequestException.class,
			() -> tokenService.reissueToken(request));
		assertEquals(ErrorCode.REFRESH_TOKEN_USER_ID_MISMATCH_ERROR, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 삭제 성공")
	void deleteRefreshToken_success() {
		// given
		Long userId = 1L;
		Token token = Token.of(userId, "refreshToken");

		when(tokenRepository.findById(userId)).thenReturn(Optional.of(token));

		// when
		tokenService.deleteRefreshToken(userId);

		// then
		verify(tokenRepository).delete(token);
	}

	@Test
	@DisplayName("토큰 삭제 실패 - 존재하지 않는 토큰")
	void deleteRefreshToken_notFound() {
		// given
		Long userId = 1L;

		when(tokenRepository.findById(userId)).thenReturn(Optional.empty());

		// when & then
		NotFoundException exception = assertThrows(NotFoundException.class,
			() -> tokenService.deleteRefreshToken(userId));
		assertEquals(ErrorCode.REFRESH_TOKEN_NOT_FOUND, exception.getErrorCode());
	}
}
