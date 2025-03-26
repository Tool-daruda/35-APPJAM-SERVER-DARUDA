package com.daruda.darudaserver.domain.tool.dto.res;

import com.daruda.darudaserver.domain.tool.entity.Category;

public record CategoryRes(String name, String koreanName) {

	public static CategoryRes from(Category category) {
		return new CategoryRes(category.name(), category.getKoreanName());
	}
}
