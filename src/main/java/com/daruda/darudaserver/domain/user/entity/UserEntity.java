package com.daruda.darudaserver.domain.user.entity;

import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.domain.user.entity.enums.SocialType;
import com.daruda.darudaserver.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class UserEntity extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Positions positions;


    @Builder
    private UserEntity(String email, String nickname, Positions positions){
        this.email = email;
        this.nickname = nickname;
        this.positions = positions;
    }

    public void updatePositions(Positions positions){
        this.positions = positions;
    }

    public void updateNickname(String nickname){
        this.nickname=nickname;
    }
}
