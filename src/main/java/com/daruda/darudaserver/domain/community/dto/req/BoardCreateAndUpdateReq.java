package com.daruda.darudaserver.domain.community.dto.req;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BoardCreateAndUpdateReq(
        @NotNull(message="제목 입력 필수 입니다.")
        @Size(min=1, max=20, message = "제목은 최소 한 글자, 최대 20글자이어야 합니다")//최소 한 글자 ,띄어쓰기 포함 최대 20자 가능
        String title,
        @Size(min=1, max=10000) //최소 한 글자 ,띄어쓰기 포함 최대 10000자 가능
        @NotNull(message="내용  입력은 필수 입니다.")
        String content,
        Long toolId,
        @NotNull(message = "자유 게시판 선택은 필수 입니다")
        boolean isFree// 자유 게시판 여부 추가
) {
        public static BoardCreateAndUpdateReq of(String title, String content, Long toolId, boolean isFree) {
                return new BoardCreateAndUpdateReq(title, content, toolId, isFree);
        }
        public static BoardCreateAndUpdateReq of(String title, String content, Long toolId) {
                return new BoardCreateAndUpdateReq(title, content, toolId, true);
        }
}