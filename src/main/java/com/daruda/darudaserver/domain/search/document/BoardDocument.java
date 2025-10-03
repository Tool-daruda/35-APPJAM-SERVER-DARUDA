package com.daruda.darudaserver.domain.search.document;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.daruda.darudaserver.domain.community.entity.Board;
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
@Document(indexName = "board")
public class BoardDocument {
	@Id
	@Field(type = FieldType.Keyword)
	private String id;

	@Field(type = FieldType.Text, analyzer = "custom_analyzer")
	private String title;

	@Field(type = FieldType.Text, analyzer = "custom_analyzer")
	private String content;

	@MultiField(mainField = @Field(type = FieldType.Text, analyzer = "custom_analyzer"),
		otherFields = {
			@InnerField(suffix = "en", type = FieldType.Text, analyzer = "english_analyzer")
		}
	)
	private String toolMainName;

	@Field(type = FieldType.Text, analyzer = "custom_analyzer")
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
			.toolMainName(tool != null ? tool.getToolMainName() : "자유")
			.toolSubName(tool != null ? tool.getToolSubName() : null)
			.toolLogo(
				tool != null ? tool.getToolLogo() : "https://daruda.s3.ap-northeast-2.amazonaws.com/Cursor_logo.png")
			.createdAt(board.getCreatedAt().toString())
			.updatedAt(board.getUpdatedAt() != null ? board.getUpdatedAt() : null)
			.imageUrl(imageUrls)
			.commentCount(commentCount)
			.isScraped(isScraped)
			.build();
	}

	public void update(final Tool tool, final String title, final String content) {
		if (tool != null) {
			this.toolMainName = tool.getToolMainName();
			this.toolSubName = tool.getToolSubName();
			this.toolId = tool.getToolId();
		} else {
			this.toolMainName = "자유";
			this.toolSubName = null;
			this.toolId = null;
		}
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
