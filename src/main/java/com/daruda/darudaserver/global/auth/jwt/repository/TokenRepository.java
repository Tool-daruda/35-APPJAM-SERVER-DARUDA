package com.daruda.darudaserver.global.auth.jwt.repository;

import com.daruda.darudaserver.global.auth.jwt.entity.Token;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TokenRepository extends CrudRepository<Token, Long> {
    Optional<Token> findByRefreshToken(final String refreshToken);

    Optional<Token> findByUserId(final Long userId);
}
