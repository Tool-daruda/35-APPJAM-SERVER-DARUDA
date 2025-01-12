package com.daruda.darudaserver.domain.user.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Positions {
    STUDENT("학생"),
    WORKER("직장인"),
    NORMAL("일반인");

    private final String name;

    public static Positions fromString(String name) {
        for (Positions position : Positions.values()) {
            if (position.getName().equals(name)) {
                return position;
            }
        }
        throw new IllegalArgumentException("Unknown position: " + name);
    }
}
