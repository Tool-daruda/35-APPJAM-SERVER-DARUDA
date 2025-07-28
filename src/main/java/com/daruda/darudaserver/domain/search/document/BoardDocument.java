package com.daruda.darudaserver.domain.search.document;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.user.entity.UserEntity;

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
@Document(indexName = "board")
public class BoardDocument {
	@Id
	@Field(type = FieldType.Keyword)
	private String id;

	@Field(type = FieldType.Text, analyzer = "custom_search_analyzerr")
	private String title;

	@Field(type = FieldType.Text, analyzer = "custom_search_analyzer")
	private String content;

	@Field(type = FieldType.Text, analyzer = "custom_search_analyzer")
	private String toolMainName;

	@Field(type = FieldType.Text, analyzer = "custom_search_analyzer")
	private String toolSubName;

	@Field(type = FieldType.Keyword)
	private String createdAt;

	@Field(type = FieldType.Long)
	private Long toolId;

	@Field(type = FieldType.Keyword)
	private List<String> imageUrl;

	@Field(type = FieldType.Keyword)
	private String author;

	@Field(type = FieldType.Keyword)
	private int commentCount;

	@Field(type = FieldType.Keyword)
	private String toolLogo;

	@Field(type = FieldType.Keyword)
	private boolean isScraped;

	@Field(type = FieldType.Date)
	private Date updatedAt;

	public static BoardDocument from(Board board, List<String> imageUrls, int commentCount, boolean isScraped) {
		Tool tool = board.getTool();

		return BoardDocument.builder()
			.content(board.getContent())
			.id(board.getId().toString())
			.title(board.getTitle())
			.author(board.getUser().getNickname())
			.toolId(tool != null ? tool.getToolId() : null)
			.toolMainName(tool != null ? tool.getToolMainName() : null)
			.toolSubName(tool != null ? tool.getToolSubName() : null)
			.toolLogo(tool != null ? tool.getToolLogo() : null)
			.createdAt(board.getCreatedAt().toString())
			.updatedAt(board.getUpdatedAt() != null ? board.getUpdatedAt() : null)
			.imageUrl(imageUrls)
			.commentCount(commentCount)
			.isScraped(isScraped)
			.build();
	}

	public void update(final Tool tool, final String title, final String content) {
		this.toolMainName = tool.getToolMainName();
		this.toolSubName = tool.getToolSubName();
		this.title = title;
		this.content = content;
	}

	public void updateCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	public void updateScraped(boolean scraped) {
		this.isScraped = scraped;
	}
}
