package com.daruda.darudaserver.domain.tool.entity;

import com.daruda.darudaserver.global.common.entity.BaseTimeEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tool")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class tool extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long toolId;

    @NotNull
    @Column(name = "tool_main_name")
    private String toolMainName;

    @NotNull
    @Column(name = "tool_sub_name")
    private String toolSubName;

    @NotNull
    @Column(name = "category")
    private Category category;

    @NotNull
    @Column(name = "tool_link")
    private String toolLink;

    @NotNull
    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "license")
    private License license;

    @NotNull
    @Column(name = "support_korea")
    private Boolean supportKorea;

    @NotNull
    @Column(name="detail_description")
    private String detailDescription;

    @Nullable
    @Column(name="plan_link")
    private String planLink;

    @NotNull
    @Column(name="color")
    private String color;

    @NotNull
    @Column(name="toolLogo")
    private String toolLogo;

}
