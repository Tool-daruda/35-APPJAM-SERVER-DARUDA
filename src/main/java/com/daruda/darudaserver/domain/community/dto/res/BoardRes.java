package com.daruda.darudaserver.domain.community.dto.res;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.List;

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
    private Boolean isScraped;
    private Long toolId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
    private Timestamp updatedAt;
    private int commentCount;

    public static BoardRes of(final Board board, final String toolName, final String toolLogo, final int commentCount, final List<String> images,final Long toolId) {
        return createBoardRes(board, toolName, toolLogo, commentCount, images, toolId);
    }
    public static BoardRes of(final Board board, final String toolName, final String toolLogo, final int commentCount, final List<String> images,final Boolean isScraped) {
        return getBoardRes(board, toolName, toolLogo, commentCount, images, isScraped);
    }
    public static BoardRes of(final Board board, final String toolName, final String toolLogo, final int commentCount, final List<String> images,final Boolean isScraped, final Long toolId) {
        return getBoardResWithToolId(board, toolName, toolLogo, commentCount, images, isScraped, toolId);
    }

    public static BoardRes createBoardRes(final Board board, final String toolName, final String toolLogo, final int commentCount, final List<String> images, final Long toolId) {
        return BoardRes.builder()
                .boardId(board.getId())
                .toolName(toolName)
                .toolLogo(toolLogo)
                .author(board.getUser().getNickname())
                .title(board.getTitle())
                .content(board.getContent())
                .images(images)
                .commentCount(commentCount)
                .updatedAt(board.getUpdatedAt())
                .isScraped(false)
                .toolId(toolId)
                .build();
    }

    public static BoardRes getBoardRes(final Board board, final String toolName, final String toolLogo, final int commentCount, final List<String> images,final Boolean isScraped) {
        return BoardRes.builder()
                .boardId(board.getId())
                .boardId(board.getId())
                .toolName(toolName)
                .toolLogo(toolLogo)
                .author(board.getUser().getNickname())
                .title(board.getTitle())
                .content(board.getContent())
                .images(images)
                .commentCount(commentCount)
                .updatedAt(board.getUpdatedAt())
                .isScraped(isScraped)
                .build();
    }

    public static BoardRes getBoardResWithToolId(final Board board, final String toolName, final String toolLogo, final int commentCount, final List<String> images,final Boolean isScraped, final Long toolId) {
        return BoardRes.builder()
                .boardId(board.getId())
                .boardId(board.getId())
                .toolName(toolName)
                .toolLogo(toolLogo)
                .author(board.getUser().getNickname())
                .title(board.getTitle())
                .content(board.getContent())
                .images(images)
                .commentCount(commentCount)
                .updatedAt(board.getUpdatedAt())
                .isScraped(isScraped)
                .toolId(toolId)
                .build();
    }


}
