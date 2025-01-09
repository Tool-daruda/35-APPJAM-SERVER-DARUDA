package com.daruda.darudaserver.domain.tool.entity;

import lombok.Getter;

@Getter
public enum Category {
    LIFESTYLE("라이프스타일"),
    DOCUMENT_EDITING("문서 편집"),
    AI("인공지능"),
    COLLABORATION("협업"),
    CODING("코딩"),
    DESIGN_MODELING("디자인/모델링"),
    DATA("데이터"),
    PRESENTATION("프레젠테이션"),
    GRAPHIC_DESIGN("그래픽 디자인"),
    VIDEO_MUSIC("영상/음악"),
    CAREER_DEVELOPMENT("커리어 개발");

    private final String koreanName;

    Category(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }
}
