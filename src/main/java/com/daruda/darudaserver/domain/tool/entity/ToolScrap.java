package com.daruda.darudaserver.domain.tool.entity;

import com.daruda.darudaserver.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tool_scrap")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ToolScrap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long toolScrapId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tool_id",nullable = false)
    private Tool tool;

}
