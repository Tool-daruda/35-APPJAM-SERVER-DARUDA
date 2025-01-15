package com.daruda.darudaserver.domain.community.dto.res;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.List;

//Private 로 하는 이유
@Builder
@Getter
public class BoardRes {
    private Long boardId;
    private String toolName;
    private String toolLogo;
    private String author;
    private String title;
    private String content;
    private List<String> images;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
    private Timestamp updatedAt;
    private int commentCount;

    public static BoardRes of(final Board board, final String toolName, final String toolLogo, final int commentCount, final List<String> images) {
        return createBoardRes(board, toolName, toolLogo, commentCount, images);
    }

    public static BoardRes createBoardRes(final Board board, final String toolName, final String toolLogo, final int commentCount, final List<String> images) {
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

}
