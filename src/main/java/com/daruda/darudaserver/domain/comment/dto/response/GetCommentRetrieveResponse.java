package com.daruda.darudaserver.domain.comment.dto.response;

import java.util.List;

import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;

public record GetCommentRetrieveResponse(
	List<GetCommentResponse> commentList,
	ScrollPaginationDto pageInfo
) {
	public static GetCommentRetrieveResponse of(List<GetCommentResponse> commentList, ScrollPaginationDto pageInfo) {
		return new GetCommentRetrieveResponse(commentList, pageInfo);
	}
}
