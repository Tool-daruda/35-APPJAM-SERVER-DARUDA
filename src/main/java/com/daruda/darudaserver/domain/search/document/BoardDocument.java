package com.daruda.darudaserver.domain.search.document;

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

	@Field(type = FieldType.Text, analyzer = "custom_analyzer")
	private String title;

	@Field(type = FieldType.Text, analyzer = "custom_analyzer")
	private String content;

	@Field(type = FieldType.Text, analyzer = "custom_analyzer", name = "tool")
	private String toolName;

	@Field(type = FieldType.Keyword)
	private String createdAt;

	@Field(type = FieldType.Long)
	private Long toolId;

	@Field(type = FieldType.Keyword)
	private List<String> imageUrl;

	public static BoardDocument from(Board board, List<String> imageUrls) {
		return BoardDocument.builder()
			.content(board.getContent())
			.id(board.getId().toString())
			.title(board.getTitle())
			.toolId(board.getTool() != null ? board.getTool().getToolId() : null)
			.toolName(board.getTool() != null ? board.getTool().getToolMainName() : null)
			.createdAt(board.getCreatedAt().toString())
			.imageUrl(imageUrls)
			.build();
	}

	public void update(final Tool tool, final UserEntity user, final String title, final String content,
		final boolean isFree) {
		this.toolName = tool.getToolMainName();
		this.title = title;
		this.content = content;
	}
}
