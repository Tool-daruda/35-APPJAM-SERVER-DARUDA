package com.daruda.darudaserver.domain.tool.entity;

import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name="tool_scrap")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ToolScrap extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long toolScrapId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tool_id",nullable = false)
    private Tool tool;

    @NotNull
    @Builder.Default
    private boolean delYn = false;

    public void update() {
        this.delYn = !this.delYn;
    }
}
