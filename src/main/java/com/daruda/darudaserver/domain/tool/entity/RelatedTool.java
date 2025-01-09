package com.daruda.darudaserver.domain.tool.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Getter
@Table(name="related_tool")
public class RelatedTool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long relatedToolId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tool_id",nullable = false)
    private Tool tool;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alternative_tool", nullable = false)
    private Tool alternativeTool;
}
