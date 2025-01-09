package com.daruda.darudaserver.domain.tool.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Getter
@Table(name="tool_platform")
public class ToolPlatForm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "platform_id")
    private Long platformId;

    @Column(name="web",nullable = false)
    private Boolean web;

    @Column(name="windows",nullable = false)
    private Boolean windows;

    @Column(name="mac",nullable = false)
    private Boolean mac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tool_id",nullable = false)
    private Tool tool;

}
