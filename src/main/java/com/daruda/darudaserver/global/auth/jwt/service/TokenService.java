package com.daruda.darudaserver.global.auth.jwt.service;

import com.daruda.darudaserver.global.auth.jwt.entity.Token;
import com.daruda.darudaserver.global.auth.jwt.repository.TokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;

    @Transactional
    public void saveRefreshtoken(final Long userId, final String refreshToken){
        tokenRepository.save(Token.of(userId, refreshToken));
    }

    public Long findIdByRefreshToken(final String refreshToken){
        Token token = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException());
        return token.getId();
    }

    @Transactional
    public void deleteRefreshToken(final Long userId){
        Token token = tokenRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException());

        tokenRepository.delete(token);
    }
}
