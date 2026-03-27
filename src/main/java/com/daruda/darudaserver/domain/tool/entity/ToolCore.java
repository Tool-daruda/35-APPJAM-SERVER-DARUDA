package com.daruda.darudaserver.domain.tool.entity;

import com.daruda.darudaserver.domain.admin.dto.request.CreateToolCoreRequest;

import jakarta.persistence.Column;
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
@Table(name = "core_feature")
public class ToolCore {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long coreId;

	@Column(name = "core_title", nullable = false)
	private String coreTitle;

	@Column(name = "core_content", nullable = false, length = 500)
	private String coreContent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id", nullable = false)
	private Tool tool;

	public static ToolCore create(CreateToolCoreRequest toolCoreRequest, Tool tool) {
		return ToolCore.builder()
			.coreTitle(toolCoreRequest.coreName())
			.coreContent(toolCoreRequest.coreContent())
			.tool(tool)
			.build();
	}
}
