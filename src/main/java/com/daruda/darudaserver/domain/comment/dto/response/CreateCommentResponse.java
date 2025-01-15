package com.daruda.darudaserver.domain.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.sql.Timestamp;

@Builder
public record CreateCommentResponse(
        Long commentId,
        String content,
        @JsonFormat(shape=JsonFormat.Shape.STRING,pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
        Timestamp updatedAt,
        String image,
        String nickname
) {
    @Builder
    public static CreateCommentResponse of(Long commentId, String content,Timestamp updatedAt, String image, String nickname){
        return CreateCommentResponse.builder()
                .commentId(commentId)
                .content(content)
                .updatedAt(updatedAt)
                .image(image)
                .nickname(nickname)
                .build();
    }
}
