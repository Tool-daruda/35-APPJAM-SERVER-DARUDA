package com.daruda.darudaserver.domain.tool.dto.response;

import com.daruda.darudaserver.domain.tool.entity.Category;

public record CategoryResponse(String name, String koreanName) {

	public static CategoryResponse from(Category category) {
		return new CategoryResponse(category.name(), category.getKoreanName());
	}
}
