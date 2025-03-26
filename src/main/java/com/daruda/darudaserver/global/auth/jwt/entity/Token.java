package com.daruda.darudaserver.global.auth.jwt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Token {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "refresh_token")
	private String refreshToken;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Builder
	public Token(Long userId, String refreshToken) {
		this.userId = userId;
		this.refreshToken = refreshToken;
	}

}
