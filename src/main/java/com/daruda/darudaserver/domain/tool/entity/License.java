package com.daruda.darudaserver.domain.tool.entity;

import lombok.Getter;

@Getter
public enum License {
    FREE("무료"),
    PARTIALLY_FREE("부분 무료"),
    PAID("유료");

    private final String koreanName;

    // 생성자
    License(String koreanName) {
        this.koreanName = koreanName;
    }

    // Getter 메서드
    public String getKoreanName() {
        return koreanName;
    }
}
