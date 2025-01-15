package com.daruda.darudaserver.domain.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.sql.Timestamp;
@Builder
public record GetCommentResponse(
        String content,
        Long commentId,
        String nickname,
        String image,
        @JsonFormat(shape=JsonFormat.Shape.STRING,pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
        Timestamp updatedAt
) {

    public static GetCommentResponse of(String content, Long commentId, String nickname,String image, Timestamp updatedAt){
        return GetCommentResponse.builder()
                .content(content)
                .commentId(commentId)
                .nickname(nickname)
                .image(image)
                .updatedAt(updatedAt)
                .build();
    }
}
