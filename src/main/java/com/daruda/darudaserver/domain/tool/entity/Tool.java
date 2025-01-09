package com.daruda.darudaserver.domain.tool.entity;

import com.daruda.darudaserver.global.common.entity.BaseTimeEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Entity
@Table(name="tool")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class Tool extends BaseTimeEntity {

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

    @Column(name="color",nullable = false)
    private String color;

    @Column(name="toolLogo",nullable = false)
    private String toolLogo;

}
