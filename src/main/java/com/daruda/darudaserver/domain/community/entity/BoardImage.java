package com.daruda.darudaserver.domain.community.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
@Entity
public class BoardImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long boardImageId;
    private final Long boardId;
    private final Long imageId;

    public static BoardImage create(final Long boardId, final Long imageId){
        return BoardImage.builder()
                .boardId(boardId)
                .imageId(imageId)
                .build();
    }
}

