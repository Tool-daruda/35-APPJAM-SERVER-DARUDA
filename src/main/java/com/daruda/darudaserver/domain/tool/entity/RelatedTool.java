package com.daruda.darudaserver.domain.tool.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Getter
@Builder
@Table(name = "related_tool")
public class RelatedTool {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long relatedToolId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id", nullable = false)
	private Tool tool;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "alternative_tool", nullable = false)
	private Tool alternativeTool;
}
