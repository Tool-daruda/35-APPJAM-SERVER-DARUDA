package com.daruda.darudaserver.global.auth.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserAuthentication extends UsernamePasswordAuthenticationToken {
	private UserAuthentication(Object principal, Object credentials,
		Collection<? extends GrantedAuthority> authorities) {
		super(principal, credentials, authorities);
	}

	public static UserAuthentication createUserAuthentication(Long userId, String role) {
		log.debug("createUserAuthentication - userId: {} role: {}", userId, role);
		if (role == null || role.isBlank()) {
			throw new IllegalArgumentException("role must not be null/blank");
		}
		String normalized = role.startsWith("ROLE_") ? role.substring(5) : role;
		SimpleGrantedAuthority authority =
			new SimpleGrantedAuthority("ROLE_" + normalized.toUpperCase(Locale.ROOT));
		return new UserAuthentication(userId, null, Collections.singletonList(authority));
	}
}
