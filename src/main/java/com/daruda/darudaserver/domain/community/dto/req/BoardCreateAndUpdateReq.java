package com.daruda.darudaserver.domain.community.dto.req;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BoardCreateAndUpdateReq(
	@NotBlank
	@Size(min = 1, max = 50, message = "제목은 최소 한 글자, 최대 50글자이어야 합니다") //최소 한 글자 ,띄어쓰기 포함 최대 50자 가능
	String title,
	@NotBlank
	@Size(min = 1, max = 10000, message = "내용  입력은 필수 입니다.") //최소 한 글자 ,띄어쓰기 포함 최대 10000자 가능
	String content,
	Long toolId,
	@NotNull(message = "자유 게시판 선택은 필수 입니다")
	boolean isFree,
	@NotNull
	List<String> imageList
) {

	public static BoardCreateAndUpdateReq of(String title, String content, Long toolId, boolean isFree,
		List<String> imageList) {
		return new BoardCreateAndUpdateReq(title, content, toolId, isFree, imageList);
	}

	public static BoardCreateAndUpdateReq of(String title, String content, Long toolId, List<String> imageList) {
		return new BoardCreateAndUpdateReq(title, content, toolId, true, imageList);
	}
}
