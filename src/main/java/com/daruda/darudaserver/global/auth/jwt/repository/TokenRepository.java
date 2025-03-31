package com.daruda.darudaserver.global.auth.jwt.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.daruda.darudaserver.global.auth.jwt.entity.Token;

public interface TokenRepository extends CrudRepository<Token, Long> {
	Optional<Token> findByRefreshToken(final String refreshToken);

	Optional<Token> findByUserId(final Long userId);
}
