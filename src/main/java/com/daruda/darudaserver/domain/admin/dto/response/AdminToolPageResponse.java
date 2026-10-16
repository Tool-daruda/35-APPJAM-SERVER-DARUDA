package com.daruda.darudaserver.domain.admin.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import com.daruda.darudaserver.domain.tool.entity.Category;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.fasterxml.jackson.annotation.JsonFormat;

public record AdminToolPageResponse(
	List<ToolResponse> tools,
	int page,
	int totalPages,
	long totalElements
) {
	public static AdminToolPageResponse of(Page<Tool> toolPage) {
		List<ToolResponse> toolResList = toolPage.getContent().stream()
			.map(tool -> new ToolResponse(
				tool.getToolId(),
				tool.getToolLogo(),
				tool.getToolMainName(),
				tool.getDescription(),
				tool.getCategory(),
				tool.getCreatedAt()
			))
			.toList();

		return new AdminToolPageResponse(
			toolResList,
			toolPage.getNumber(),
			toolPage.getTotalPages(),
			toolPage.getTotalElements()
		);
	}

	public record ToolResponse(
		Long toolId,
		String toolLogo,
		String toolName,
		String description,
		Category category,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
		LocalDateTime createdAt
	) {
	}
}

