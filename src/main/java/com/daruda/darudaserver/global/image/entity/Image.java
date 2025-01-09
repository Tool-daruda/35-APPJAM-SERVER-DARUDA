package com.daruda.darudaserver.global.image.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @Column(name="image_url",nullable = false)
    private String imageUrl;


    @Builder
    public Image(final Long imageId, final String imageUrl){
        this.imageId=imageId;
        this.imageUrl = imageUrl;
    }

}
