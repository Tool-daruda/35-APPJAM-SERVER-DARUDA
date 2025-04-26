package com.daruda.darudaserver.global.auth.jwt.service;

import org.springframework.stereotype.Service;

import com.daruda.darudaserver.domain.user.dto.response.JwtTokenResponse;
import com.daruda.darudaserver.global.auth.jwt.entity.Token;
import com.daruda.darudaserver.global.auth.jwt.provider.JwtTokenProvider;
import com.daruda.darudaserver.global.auth.jwt.provider.JwtValidationType;
import com.daruda.darudaserver.global.auth.jwt.repository.TokenRepository;
import com.daruda.darudaserver.global.auth.security.UserAuthentication;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.InvalidValueException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.error.exception.UnauthorizedException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

	private final TokenRepository tokenRepository;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public JwtTokenResponse createToken(final Long userId) {
		UserAuthentication userAuthentication = UserAuthentication.createUserAuthentication(userId);

		String accessToken = jwtTokenProvider.generateAccessToken(userAuthentication);
		log.debug("AccessToken을 정상적으로 생성하였습니다, {}", accessToken);

		String refreshToken = updateRefreshToken(userId, userAuthentication);
		log.debug("RefreshToken을 정상적으로 생성하였습니다, {}", refreshToken);

		return JwtTokenResponse.of(accessToken, refreshToken);
	}

	@Transactional
	public JwtTokenResponse reissueToken(final String refreshToken) {
		validateRefreshToken(refreshToken);

		Long userId = jwtTokenProvider.getUserIdFromJwt(refreshToken);

		verifyUserIdWithStoredToken(userId, refreshToken);

		return createToken(userId);
	}

	@Transactional
	public void deleteRefreshToken(final Long userId) {
		Token token = tokenRepository.findByUserId(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

		tokenRepository.delete(token);
	}

	private Long findIdByRefreshToken(final String refreshToken) {
		Token token = tokenRepository.findByRefreshToken(refreshToken)
			.orElseThrow(() -> new InvalidValueException(ErrorCode.REFRESH_TOKEN_EMPTY_ERROR));
		return token.getUserId();
	}

	private String updateRefreshToken(final Long userId, final UserAuthentication userAuthentication) {
		tokenRepository.findByUserId(userId).ifPresent(tokenRepository::delete);

		String refreshToken = jwtTokenProvider.generateRefreshToken(userAuthentication);

		tokenRepository.save(Token.of(userId, refreshToken));

		return refreshToken;
	}

	private void validateRefreshToken(final String refreshToken) {
		JwtValidationType validationType = jwtTokenProvider.validateToken(refreshToken);

		if (!validationType.equals(JwtValidationType.VALID_JWT)) {
			throw switch (validationType) {
				case EXPIRED_JWT_TOKEN -> new UnauthorizedException(ErrorCode.REFRESH_TOKEN_EXPIRED_ERROR);
				case INVALID_JWT_TOKEN -> new BadRequestException(ErrorCode.INVALID_REFRESH_TOKEN_ERROR);
				case INVALID_JWT_SIGNATURE -> new BadRequestException(ErrorCode.REFRESH_TOKEN_SIGNATURE_ERROR);
				case UNSUPPORTED_JWT_TOKEN -> new BadRequestException(ErrorCode.UNSUPPORTED_REFRESH_TOKEN_ERROR);
				case EMPTY_JWT -> new BadRequestException(ErrorCode.REFRESH_TOKEN_EMPTY_ERROR);
				default -> new BusinessException(ErrorCode.UNKNOWN_REFRESH_TOKEN_ERROR);
			};
		}
	}

	private void verifyUserIdWithStoredToken(final Long userId, final String refreshToken) {
		Long storedUserId = findIdByRefreshToken(refreshToken);

		if (!storedUserId.equals(userId)) {
			throw new BadRequestException(ErrorCode.REFRESH_TOKEN_USER_ID_MISMATCH_ERROR);
		}
	}
}
