package com.daruda.darudaserver.domain.tool.entity;

import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.InvalidValueException;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Category {
    ALL("전체"),
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

    public static Category fromKoreanName(String name) {
        // Null 또는 빈 문자열의 경우 기본값으로 Category.ALL 반환
        if (name == null || name.isBlank()) {
            return Category.ALL;
        }
        // 한국어 이름을 기반으로 Category 검색
        return Arrays.stream(values())
                .filter(category -> category.koreanName.equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElseThrow(() -> new InvalidValueException(ErrorCode.INVALID_TOOL_CATEGORY));
    }


    public static Category fromEnglishName(String name) {
        if (name == null || name.isBlank()) {
            return Category.ALL;
        }
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElseThrow(() -> new InvalidValueException(ErrorCode.INVALID_TOOL_CATEGORY));
    }
}
