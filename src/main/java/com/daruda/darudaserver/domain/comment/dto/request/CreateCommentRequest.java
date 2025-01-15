package com.daruda.darudaserver.domain.comment.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @Size(min = 0,max = 1000, message = "글자 수는 1자 이상 1000자 이하여야 합니다")
        String content,
        @Nullable
        String image
) {
}
