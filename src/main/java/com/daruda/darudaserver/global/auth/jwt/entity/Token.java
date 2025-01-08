package com.daruda.darudaserver.global.auth.jwt.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String refreshToken;

    public static Token of(final Long id, final String refreshToken){
        return Token.builder()
                .id(id)
                .refreshToken(refreshToken)
                .build();
    }
}
