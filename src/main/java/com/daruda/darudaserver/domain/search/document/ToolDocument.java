package com.daruda.darudaserver.domain.search.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.daruda.darudaserver.domain.tool.entity.Tool;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Setting(settingPath = "/elasticsearch/custom-settings.json")
@Document(indexName = "tool")
public class ToolDocument {

	@Id
	@Field(type = FieldType.Keyword)
	private String id;

	@MultiField(mainField = @Field(type = FieldType.Text, analyzer = "custom_analyzer"),
		otherFields = {
			@InnerField(suffix = "en", type = FieldType.Text, analyzer = "english_analyzer")
		})
	private String toolMainName;

	@Field(type = FieldType.Text, analyzer = "custom_analyzer")
	private String toolSubName;

	@Field(type = FieldType.Keyword)
	private String category;

	@Field(type = FieldType.Text)
	private String toolLink;

	@Field(type = FieldType.Text, analyzer = "custom_analyzer")
	private String description;

	@Field(type = FieldType.Keyword)
	private String license;

	@Field(type = FieldType.Boolean)
	private Boolean supportKorea;

	@Field(type = FieldType.Text, analyzer = "custom_analyzer")
	private String detailDescription;

	@Field(type = FieldType.Text)
	private String planLink;

	@Field(type = FieldType.Keyword)
	private String bgColor;

	@Field(type = FieldType.Boolean)
	private boolean fontColor;

	@Field(type = FieldType.Integer)
	private int viewCount;

	@Field(type = FieldType.Integer)
	private int popular;

	@Field(type = FieldType.Keyword)
	private String toolLogo;

	public static ToolDocument from(Tool tool) {
		return ToolDocument.builder()
			.id(tool.getToolId().toString())
			.toolMainName(tool.getToolMainName())
			.toolSubName(tool.getToolSubName())
			.category(tool.getCategory().name())
			.toolLink(tool.getToolLink())
			.description(tool.getDescription())
			.license(tool.getLicense().name())
			.supportKorea(tool.getSupportKorea())
			.detailDescription(tool.getDetailDescription())
			.planLink(tool.getPlanLink())
			.bgColor(tool.getBgColor())
			.fontColor(tool.isFontColor())
			.viewCount(tool.getViewCount())
			.popular(tool.getPopular())
			.toolLogo(tool.getToolLogo())
			.build();
	}
}
