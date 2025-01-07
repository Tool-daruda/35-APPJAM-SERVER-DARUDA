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
    private long imageId;

    @Column(name="folder",nullable = false)
    private  String folder;

    @Column(name="original_name", nullable = false)
    private  String originalName;

    @Column(name="stored_name", nullable = false)
    private  String storedName;

    @Column(name="del_yn",nullable = false)
    private boolean delYn = false;

    @Column(name="updated_at",nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate // 엔터티가 저장되기 전에 호출하기 위함
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public Image(final String folder, final String originalName, final String storedName){
        this.folder = folder;
        this.originalName=originalName;
        this.storedName =storedName;
    }

    public void delete(){
        this.delYn = true;
    }
}
