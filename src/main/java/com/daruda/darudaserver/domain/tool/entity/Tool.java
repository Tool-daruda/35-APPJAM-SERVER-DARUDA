package com.daruda.darudaserver.domain.tool.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tool")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class Tool {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long toolId;

	@Column(name = "tool_main_name", nullable = false)
	private String toolMainName;

	@Column(name = "tool_sub_name", nullable = false)
	private String toolSubName;

	@Enumerated(EnumType.STRING)
	@Column(name = "category", nullable = false)
	private Category category;

	@Column(name = "tool_link", nullable = false, length = 5000)
	private String toolLink;

	@Column(name = "description", nullable = false)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "license", nullable = false)
	private License license;

	@Column(name = "support_korea", nullable = false)
	private Boolean supportKorea;

	@Column(name = "detail_description", nullable = false)
	private String detailDescription;

	@Column(name = "plan_link", length = 5000)
	private String planLink;

	@Column(name = "bg_color", nullable = false)
	private String bgColor;

	@Column(name = "font_color")
	private boolean fontColor;

	@Column(name = "tool_logo", nullable = false)
	private String toolLogo;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Timestamp createdAt;

	@Column(name = "updated_at")
	private Timestamp updatedAt;

	@Column(columnDefinition = "integer default 0", nullable = false)
	private int viewCount;

	@Column(name = "popular", columnDefinition = "integer default 0")
	private int popular;

	public void incrementViewCount() {
		this.viewCount++;
	}

	public void updatePopular(int scrapCount) {
		this.popular = scrapCount * 10 + this.viewCount;
	}

	public String upperMainName(String toolMainName) {
		//첫 글자가 대문자이면 그대로 return
		if (Character.isUpperCase(toolMainName.charAt(0))) {
			return toolMainName;
		}
		//첫 글자가 대문자가 아닌 경우에만 대문자로 변경
		String uppdatedName = toolMainName.substring(0, 1).toUpperCase() + toolMainName.substring(1);

		return uppdatedName;
	}

}
