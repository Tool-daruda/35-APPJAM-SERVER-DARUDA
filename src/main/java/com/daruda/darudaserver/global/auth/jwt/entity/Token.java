package com.daruda.darudaserver.global.auth.jwt.entity;

import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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
