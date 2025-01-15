package com.daruda.darudaserver.domain.tool.entity;


import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name="tool")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class Tool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long toolId;

    @Column(name = "tool_main_name", nullable = false)
    private String toolMainName;

    @Column(name = "tool_sub_name", nullable = false)
    private String toolSubName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Column(name = "tool_link", nullable = false)
    private String toolLink;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "license" , nullable = false)
    private License license;

    @Column(name = "support_korea", nullable = false)
    private Boolean supportKorea;

    @Column(name="detail_description",nullable = false)
    private String detailDescription;

    @Column(name="plan_link")
    private String planLink;

    @Column(name="bg_color",nullable = false)
    private String bgColor;

    @Column(name="font_color")
    private boolean fontColor;


    @Column(name="tool_logo",nullable = false)
    private String toolLogo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(columnDefinition="integer default 0",nullable = false)
    private int viewCount;

    // createdAt 값을 설정하는 메서드
    @PrePersist
    protected void onCreate() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    // viewCount를 업데이트하는 메서드 (updatedAt에 영향 없음)
    public void incrementViewCount() {
        this.viewCount++;
    }
}
