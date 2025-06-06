package com.daruda.darudaserver.global.auth.jwt.entity;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@RedisHash(value = "refreshToken", timeToLive = 604800000)
public class Token {

	@Id
	private Long id;

	@Indexed
	private String refreshToken;

	public static Token of(final Long id, final String refreshToken) {
		return Token.builder()
			.id(id)
			.refreshToken(refreshToken)
			.build();
	}
}
