package com.daruda.darudaserver.global.auth.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserAuthentication extends UsernamePasswordAuthenticationToken {
	private UserAuthentication(Object principal, Object credentials,
		Collection<? extends GrantedAuthority> authorities) {
		super(principal, credentials, authorities);
	}

	public static UserAuthentication createUserAuthentication(Long userId) {
		log.debug("createUserAuthentication - userId" + userId);
		return new UserAuthentication(userId, null, Collections.emptyList());
	}
}
