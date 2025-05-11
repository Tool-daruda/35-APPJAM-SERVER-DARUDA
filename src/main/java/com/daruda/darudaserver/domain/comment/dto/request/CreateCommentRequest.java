package com.daruda.darudaserver.domain.comment.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
	@Nullable
	@Size(min = 1, max = 1000, message = "글자 수는 1자 이상 1000자 이하여야 합니다")
	String content,

	@Nullable
	String photoUrl
) {
	@AssertTrue(message = "content와 photoUrl 중 하나는 반드시 입력해야 합니다.")
	private boolean isValid() {
		return (content != null && !content.isBlank())
			||
			(photoUrl != null && !photoUrl.isBlank());
	}
}
