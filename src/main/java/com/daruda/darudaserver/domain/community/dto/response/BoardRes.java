package com.daruda.darudaserver.domain.community.dto.response;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

//Private 로 하는 이유
@Builder(access = AccessLevel.PRIVATE)
public record BoardRes(
        Long boardId,
        Long toolId,
        String title,
        String content,
        List<String> images,
        @JsonFormat(shape=JsonFormat.Shape.STRING,pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
        Timestamp updatedAt
) {
    public static BoardRes of(final Board board,final List<String> images){
        return BoardRes.builder()
                .boardId(board.getBoardId())
                .toolId(board.getToolId())
                .title(board.getTitle())
                .content(board.getContent())
                .images(images)
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    public static BoardRes of(final Board board){
        return BoardRes.builder()
                .boardId(board.getBoardId())
                .toolId(board.getToolId())
                .title(board.getTitle())
                .content(board.getContent())
                .updatedAt(board.getUpdatedAt())
                .images(null)
                .build();
    }
}
