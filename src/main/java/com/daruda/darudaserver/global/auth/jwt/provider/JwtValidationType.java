package com.daruda.darudaserver.global.auth.jwt.provider;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum JwtValidationType {
	VALID_JWT("VALID_JWT"),
	INVALID_JWT_SIGNATURE("INVALID_JWT_SIGNATURE"),
	INVALID_JWT_TOKEN("INVALID_JWT_TOKEN"),

	EXPIRED_JWT_TOKEN("EXPIRED_JWT_TOKEN"),
	UNSUPPORTED_JWT_TOKEN("UNSUPPORTED_JWT_TOKEN"),
	EMPTY_JWT("EMPTY_JWT");

	private String validationType;
}
