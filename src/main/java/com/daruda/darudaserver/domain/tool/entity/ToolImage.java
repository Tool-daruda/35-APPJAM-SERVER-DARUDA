package com.daruda.darudaserver.domain.tool.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name="tool_image")
public class ToolImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="tool_image_id")
    private Long imageId;

    @Column(name="image_url",nullable = false)
    private String imageUrl;

    @Column(name="image_order", nullable = false)
    private int imageOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tool_id",nullable = false)
    private Tool toolId;
}
