package com.daruda.darudaserver.domain.community.dto.response;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

//Private 로 하는 이유
@Builder(access = AccessLevel.PRIVATE)
public record BoardRes(
        Long boardId,
        Long toolId,
        String title,
        String content,
        List<String> imagesUrl,
        @JsonFormat(shape=JsonFormat.Shape.STRING,pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
        LocalDateTime createdDate
) {
    public static BoardRes of(Board board,List<String> imagesUrl){
        return BoardRes.builder()
                .boardId(board.getBoardId())
                .toolId(board.getToolId())
                .title(board.getTitle())
                .content(board.getContent())
                .imagesUrl(imagesUrl)
                .createdDate(board.getCreatedAt())
                .build();
    }
}
