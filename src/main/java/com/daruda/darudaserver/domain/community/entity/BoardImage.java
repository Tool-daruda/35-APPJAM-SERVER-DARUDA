package com.daruda.darudaserver.domain.community.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class BoardImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long boardImageId;
    private  Long boardId;
    private  Long imageId;

    public static BoardImage create(final Long boardId, final Long imageId){
        return BoardImage.builder()
                .boardId(boardId)
                .imageId(imageId)
                .build();
    }
}

