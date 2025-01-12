package com.daruda.darudaserver.domain.user.entity;

import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.domain.user.entity.enums.SocialType;
import com.daruda.darudaserver.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "user")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private String email;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Positions positions;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private SocialType socialType = SocialType.KAKAO;

    @Builder
    private UserEntity(String email, String nickname, Positions positions, LocalDateTime createdAt, SocialType socialType){
        this.email = email;
        this.nickname = nickname;
        this.positions = positions;
        this.createdAt = createdAt;
        this.socialType = socialType;
    }
}
