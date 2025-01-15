package com.daruda.darudaserver.domain.community.dto.res;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;

import java.sql.Timestamp;
import java.util.List;

//Private 로 하는 이유
@Builder(access = AccessLevel.PRIVATE)
public record BoardRes(
        Long boardId,
        String toolName,
        String toolLogo,
        String author,
        String title,
        String content,
        List<String> images,
        @JsonFormat(shape=JsonFormat.Shape.STRING,pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
        Timestamp updatedAt,
        int commentCount
) {
    // Image 가 있는 경우
    public static BoardRes of(final Board board,final String toolName, final String toolLogo,  final int commentCount,final List<String> images ){
        return BoardRes.builder()
                .boardId(board.getBoardId())
                .toolName(toolName)
                .toolLogo(toolLogo)
                .author(board.getUser().getNickname())
                .title(board.getTitle())
                .content(board.getContent())
                .images(images)
                .updatedAt(board.getUpdatedAt())
                .commentCount(commentCount)
                .build();
    }

    //Image 가 없는 경우
    public static BoardRes of(final Board board, final String toolName, final String toolLogo,final int commentCount){
        return BoardRes.builder()
                .boardId(board.getBoardId())
                .toolName(toolName)
                .toolLogo(toolLogo)
                .author(board.getUser().getNickname())
                .title(board.getTitle())
                .content(board.getContent())
                .images(null)
                .updatedAt(board.getUpdatedAt())
                .commentCount(commentCount)
                .build();
    }
}
