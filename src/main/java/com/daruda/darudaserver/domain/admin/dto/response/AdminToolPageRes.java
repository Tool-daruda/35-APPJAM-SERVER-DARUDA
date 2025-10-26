package com.daruda.darudaserver.domain.admin.dto.response;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;

import com.daruda.darudaserver.domain.tool.entity.Category;
import com.daruda.darudaserver.domain.tool.entity.Tool;

public record AdminToolPageRes(
	List<ToolRes> tools,
	int page,
	int totalPages,
	long totalElements
) {
	public static AdminToolPageRes of(Page<Tool> toolPage) {
		List<ToolRes> toolResList = toolPage.getContent().stream()
			.map(tool -> new ToolRes(
				tool.getToolId(),
				tool.getToolLogo(),
				tool.getToolMainName(),
				tool.getDescription(),
				tool.getCategory(),
				tool.getCreatedAt()
			))
			.toList();

		return new AdminToolPageRes(
			toolResList,
			toolPage.getNumber(),
			toolPage.getTotalPages(),
			toolPage.getTotalElements()
		);
	}

	public record ToolRes(
		Long toolId,
		String toolLogo,
		String toolName,
		String description,
		Category category,
		Timestamp createdAt
	) {
	}
}

