package com.daruda.darudaserver.domain.comment.dto.response;

import java.util.List;

public record GetCommentRetrieveResponse(
        List<GetCommentResponse> commentList,
        PagenationDto pagenationDto
) {
   public static GetCommentRetrieveResponse of(List<GetCommentResponse> commentList, PagenationDto pagenationDto){
       return new GetCommentRetrieveResponse(commentList, pagenationDto);
   }
}
