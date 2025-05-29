package com.daruda.darudaserver.domain.search.document;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.global.image.entity.Image;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Setting(settingPath = "src/main/resources/elasticsearch/custom-settings.json")
@Document(indexName = "board")
public class BoardDocument {
	@Id
	@Field(type = FieldType.Keyword)
	private Long id;

	@Field(type = FieldType.Text, analyzer = "custom_analyzer")
	private String title;

	@Field(type = FieldType.Text, analyzer = "custom_analyzer")
	private String content;

	@Field(type = FieldType.Keyword, analyzer = "custom_analyzer")
	private String toolName;

	@Field(type = FieldType.Keyword)
	private String createdAt;

	@Field(type = FieldType.Long)
	private Long toolId;

	@Field(type = FieldType.Keyword)
	private String imageUrl;

	public static BoardDocument from(Board board, Image image) {
		return BoardDocument.builder()
			.content(board.getContent())
			.id(board.getId())
			.title(board.getTitle())
			.toolId(board.getTool().getToolId())
			.toolName(board.getTool().getToolMainName())
			.createdAt(board.getCreatedAt().toString())
			.imageUrl(image.getImageUrl())
			.build();
	}
}
