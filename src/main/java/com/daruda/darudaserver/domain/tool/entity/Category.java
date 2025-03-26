package com.daruda.darudaserver.domain.tool.entity;

import lombok.Getter;

@Getter
public enum Category {
	ALL("전체"),
	AI("AI"),
	DOCUMENT_EDITING("문서 작성&편집"),
	PRESENTATION("프레젠테이션"),
	COLLABORATION("협업&커뮤니케이션"),
	DATA("데이터"),
	GRAPHIC_DESIGN("그래픽&디자인"),
	VIDEO_MUSIC("영상&음악"),
	CODING("코딩&개발"),
	DESIGN_MODELING("설계&모델링"),
	LIFESTYLE("생활"),
	CAREER_DEVELOPMENT("커리어&자기개발");

	private final String koreanName;

	Category(String koreanName) {
		this.koreanName = koreanName;
	}
}

