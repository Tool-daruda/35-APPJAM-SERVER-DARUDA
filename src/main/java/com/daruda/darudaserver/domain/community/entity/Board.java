package com.daruda.darudaserver.domain.community.entity;

import com.daruda.darudaserver.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Entity
@RequiredArgsConstructor
public class Board extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long boardId;

    @NotNull
    private final String title;

    @NotNull
    private final String content;

    @NotNull
    private final boolean delYn;

    @NotNull
    private final Long userId;

}
