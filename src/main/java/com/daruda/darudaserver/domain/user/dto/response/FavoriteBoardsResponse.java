package com.daruda.darudaserver.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.sql.Timestamp;


@Builder
public record FavoriteBoardsResponse(
        Long boardId,
        String title,
        String content,
        @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy.MM.dd",timezone = "Asia/Seoul")
        Timestamp updatedAt,
        String toolName,
        String toolLogo,
        Boolean isScrapped
) {
    public static FavoriteBoardsResponse of(Long boardId, String title, String content, Timestamp updatedAt, String toolName, String toolLogo, Boolean isScrapped){
        return FavoriteBoardsResponse.builder()
                .boardId(boardId)
                .title(title)
                .content(content)
                .updatedAt(updatedAt)
                .toolName(toolName)
                .toolLogo(toolLogo)
                .isScrapped(isScrapped)
                .build();
    }
}
