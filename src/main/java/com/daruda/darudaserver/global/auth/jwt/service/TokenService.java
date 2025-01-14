package com.daruda.darudaserver.global.auth.jwt.service;

import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.global.auth.jwt.entity.Token;
import com.daruda.darudaserver.global.auth.jwt.repository.TokenRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.InvalidValueException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;


    @Transactional
    public void saveRefreshtoken(final Long userId, final String refreshToken){
        tokenRepository.save(Token.builder()
                .userId(userId)
                .refreshToken(refreshToken)
                .build());
    }

    @Transactional
    public Long findIdByRefreshToken(final String refreshToken){
        Token token = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidValueException(ErrorCode.REFRESH_TOKEN_EMPTY_ERROR));
        return token.getUserId();
    }

    @Transactional
    public void deleteRefreshToken(final Long userId){
        Token token = tokenRepository.findByUserId(userId)
                .orElseThrow(()-> new NotFoundException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        tokenRepository.delete(token);
    }

    @Transactional
    public String getRefreshTokenByUserId(Long userId)
    {
        Token token = tokenRepository.findByUserId(userId)
                .orElseThrow(()->new NotFoundException(ErrorCode.USER_NOT_FOUND));
        return token.getRefreshToken();
    }

}
