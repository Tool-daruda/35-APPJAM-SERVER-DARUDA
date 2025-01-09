package com.daruda.darudaserver.domain.tool.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Getter
@Table(name="core_feature")
public class ToolCore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long coreId;

    @Column(name="core_title",nullable = false)
    private String coreTitle;

    @Column(name="core_content",nullable = false)
    private String coreContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tool_id",nullable = false)
    private Tool tool;
}
