package com.daruda.darudaserver.domain.tool.entity;

import java.sql.Timestamp;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class Tool {

	@Column(name = "plan_type")
	PlanType planType;
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
	@Column(name = "description", nullable = false, length = 500)
	private String description;
	@Enumerated(EnumType.STRING)
	@Column(name = "license", nullable = false)
	private License license;
	@Column(name = "support_korea", nullable = false)
	private Boolean supportKorea;
	@Column(name = "detail_description", nullable = false, length = 500)
	private String detailDescription;
	@Column(name = "plan_link", length = 5000)
	private String planLink;
	@Column(name = "bg_color", nullable = false)
	private String bgColor;
	@Column(name = "font_color")
	private boolean fontColor;
	@Column(name = "tool_logo", nullable = false)
	private String toolLogo;
	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private Timestamp createdAt;
	@LastModifiedDate
	@Column(name = "updated_at")
	private Timestamp updatedAt;
	@Column(columnDefinition = "integer default 0", nullable = false)
	private int viewCount;
	@Column(name = "popular", columnDefinition = "integer default 0")
	private int popular;

	@Builder
	private Tool(String toolMainName, String toolSubName, Category category, String toolLink, String description,
		License license, Boolean supportKorea, String detailDescription, String planLink, String bgColor,
		boolean fontColor, String toolLogo) {
		this.toolMainName = toolMainName;
		this.toolSubName = toolSubName;
		this.category = category;
		this.toolLink = toolLink;
		this.description = description;
		this.license = license;
		this.supportKorea = supportKorea;
		this.detailDescription = detailDescription;
		this.planLink = planLink;
		this.bgColor = bgColor;
		this.fontColor = fontColor;
		this.toolLogo = toolLogo;
	}

	public static Tool of(String toolMainName, String toolSubName, Category category, String toolLink,
		String description,
		License license, Boolean supportKorea, String detailDescription, String planLink, String bgColor,
		boolean fontColor, String toolLogo) {
		return Tool.builder()
			.toolMainName(toolMainName)
			.toolSubName(toolSubName)
			.category(category)
			.toolLink(toolLink)
			.description(description)
			.license(license)
			.supportKorea(supportKorea)
			.detailDescription(detailDescription)
			.planLink(planLink)
			.bgColor(bgColor)
			.fontColor(fontColor)
			.toolLogo(toolLogo)
			.build();
	}

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

	public void update(final String toolMainName,
		final String toolSubName,
		final Category category,
		final String toolLink,
		final String description,
		final License license,
		final Boolean supportKorea,
		final String detailDescription,
		final String planLink,
		final String bgColor,
		final Boolean fontColor,
		final String toolLogo,
		final PlanType planType) {
		this.toolMainName = toolMainName;
		this.toolSubName = toolSubName;
		this.category = category;
		this.toolLink = toolLink;
		this.description = description;
		this.license = license;
		this.supportKorea = supportKorea;
		this.detailDescription = detailDescription;
		this.planLink = planLink;
		this.bgColor = bgColor;
		if (fontColor != null) {
			this.fontColor = fontColor;
		}
		this.toolLogo = toolLogo;
		this.planType = planType;
	}

}
