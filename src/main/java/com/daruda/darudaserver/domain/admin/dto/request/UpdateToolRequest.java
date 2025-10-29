package com.daruda.darudaserver.domain.admin.dto.request;

import java.util.List;

import org.springframework.context.annotation.Description;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateToolRequest(
	@Description("영문 이름")
	String toolMainName,
	@Description("한글 이름")
	String toolSubName,
	@Description("카테고리")
	String category,
	@Description("바로가기 링크")
	String toolLink,
	@Description("한줄 소개")
	@Size(max = 500, message = "한줄 소개는 500자 이내로 작성해주세요.")
	String description,
	@Description("라이센스")
	String license,
	@Description("한국어 지원 여부")
	Boolean supportKorea,
	@Description("소개 글")
	@Size(max = 500, message = "소개 글은 500자 이내로 작성해주세요.")
	String detailDescription,
	@Description("가격정책 링크")
	String planLink,
	@Description("배경 색상")
	String bgColor,
	@Description("글자 색상")
	Boolean fontColor,
	@Description("로고 이미지 링크")
	String toolLogo,
	@Description("플랫폼 지원 정보")
	@Nullable CreateToolPlatFormRequest toolPlatForm,
	@Description("키워드 목록")
	List<String> keywords,
	@Description("핵심 기능 목록")
	List<CreateToolCoreRequest> cores,
	@Description("플랜 목록")
	List<CreateToolPlanRequest> plans,
	@Description("블로그 링크 목록")
	List<String> blogLinks,
	@Description("툴 소개 이미지 링크 목록")
	List<String> images,
	@Description("참고하면 좋은 영상 링크 목록")
	List<String> videos,
	@Description("관련 툴 ID 목록")
	List<Integer> relatedToolIds
) {
}
