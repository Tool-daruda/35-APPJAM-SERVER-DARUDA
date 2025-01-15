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
        Long toolId,
        String toolLogo,
        Long scarpId
) {
    public static FavoriteBoardsResponse of(Long boardId, String title, String content, Timestamp updatedAt, Long toolId, String toolLogo, Long scarpId){
        return FavoriteBoardsResponse.builder()
                .boardId(boardId)
                .title(title)
                .content(content)
                .updatedAt(updatedAt)
                .toolId(toolId)
                .toolLogo(toolLogo)
                .scarpId(scarpId)
                .build();
    }
}
