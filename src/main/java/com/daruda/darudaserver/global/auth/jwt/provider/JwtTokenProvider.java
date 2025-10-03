package com.daruda.darudaserver.global.auth.jwt.provider;

import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.UnauthorizedException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.access-token-expire-time}")
	private long accessTokenExpireTime;

	@Value("${jwt.refresh-token-expire-time}")
	private long refreshTokenExpireTime;

	private static final String USER_ID = "userId";

	//    @PostConstruct
	//    protected void init(){
	//        jwtSecret = Base64.getEncoder().encodeToString(jwtSecret.getBytes(StandardCharsets.UTF_8));
	//    }

	public String generateAccessToken(final Authentication authentication) {
		return generateToken(authentication, accessTokenExpireTime);
	}

	public String generateRefreshToken(final Authentication authentication) {
		return generateToken(authentication, refreshTokenExpireTime);
	}

	public Long getUserIdFromJwt(String token) {
		log.debug("JWT 파싱 시작: {}", token);
		Claims claims = getBody(token);
		Long userId = Long.valueOf(claims.get(USER_ID).toString());
		log.debug("JWT 파싱 성공 - UserID: {}", userId);
		return userId;
	}

	private Claims getBody(final String token) {
		try {
			return Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.setAllowedClockSkewSeconds(60)
				.build()
				.parseClaimsJws(token)
				.getBody();
		} catch (Exception e) {
			log.error("JWT 파싱 실패: {}", e.getMessage(), e);
			throw e;
		}
	}

	private SecretKey getSigningKey() {
		String encodedKey = Base64.getEncoder().encodeToString(jwtSecret.getBytes());
		SecretKey key = Keys.hmacShaKeyFor(encodedKey.getBytes());
		return key;
	}

	private String generateToken(final Authentication authentication, final long expiredTime) {
		final Date now = new Date();

		//claim 생성 및 토큰 만료 시간 설정(현재 시간 + yml 파일에 설정한 expire time)
		final Claims claims = Jwts.claims().setIssuedAt(now).setExpiration(new Date(now.getTime() + expiredTime));

		//userId claim에 저장
		claims.put(USER_ID, authentication.getPrincipal());

		return Jwts.builder()
			.setHeaderParam(Header.TYPE, Header.JWT_TYPE)
			.setClaims(claims)
			.signWith(getSigningKey())
			.compact();
	}

	public JwtValidationType validateToken(String token) {
		try {
			Claims claims = getBody(token);
			return JwtValidationType.VALID_JWT;
		} catch (MalformedJwtException ex) {
			return JwtValidationType.INVALID_JWT_TOKEN;
		} catch (ExpiredJwtException ex) {
			return JwtValidationType.EXPIRED_JWT_TOKEN;
		} catch (UnsupportedJwtException ex) {
			return JwtValidationType.UNSUPPORTED_JWT_TOKEN;
		} catch (IllegalArgumentException ex) {
			return JwtValidationType.EMPTY_JWT;
		} catch (SignatureException ex) {
			return JwtValidationType.INVALID_JWT_TOKEN;
		}
	}

	public void validateRefreshToken(final String refreshToken) {
		JwtValidationType jwtValidationType = validateToken(refreshToken);

		if (!jwtValidationType.equals(JwtValidationType.VALID_JWT)) {
			throw switch (jwtValidationType) {
				case EXPIRED_JWT_TOKEN -> new UnauthorizedException(ErrorCode.REFRESH_TOKEN_EXPIRED_ERROR);
				case INVALID_JWT_TOKEN -> new BadRequestException(ErrorCode.INVALID_REFRESH_TOKEN_ERROR);
				case INVALID_JWT_SIGNATURE -> new BadRequestException(ErrorCode.REFRESH_TOKEN_SIGNATURE_ERROR);
				case UNSUPPORTED_JWT_TOKEN -> new BadRequestException(ErrorCode.UNSUPPORTED_REFRESH_TOKEN_ERROR);
				case EMPTY_JWT -> new BadRequestException(ErrorCode.REFREH_TOKEN_EMPTY_ERROR);
				default -> new BusinessException(ErrorCode.UNSUPPORTED_REFRESH_TOKEN_ERROR);
			};
		}
	}

	public boolean validateTokens(String jwtToken) {
		try {
			return !getBody(jwtToken).getExpiration().before(new Date());
		} catch (Exception e) {
			return false;
		}
	}

}
