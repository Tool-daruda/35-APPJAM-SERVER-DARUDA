package com.daruda.darudaserver.domain.admin.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateToolRequest(
	@Schema(description = "영문 이름")
	String toolMainName,
	@Schema(description = "한글 이름")
	String toolSubName,
	@Schema(description = "카테고리")
	String category,
	@Schema(description = "바로가기 링크")
	String toolLink,
	@Schema(description = "한줄 소개")
	@Size(max = 500, message = "한줄 소개는 500자 이내로 작성해주세요.")
	String description,
	@Schema(description = "라이센스")
	String license,
	@Schema(description = "한국어 지원 여부")
	Boolean supportKorea,
	@Schema(description = "소개 글")
	@Size(max = 500, message = "소개 글은 500자 이내로 작성해주세요.")
	String detailDescription,
	@Schema(description = "가격정책 링크")
	String planLink,
	@Schema(description = "배경 색상")
	String bgColor,
	@Schema(description = "글자 색상")
	Boolean fontColor,
	@Schema(description = "로고 이미지 링크")
	String toolLogo,
	@Schema(description = "플랫폼 지원 정보")
	@Nullable CreateToolPlatFormRequest toolPlatForm,
	@Schema(description = "키워드 목록")
	List<String> keywords,
	@Schema(description = "핵심 기능 목록")
	List<CreateToolCoreRequest> cores,
	@Schema(description = "플랜 유형")
	String planType,
	@Schema(description = "플랜 목록")
	List<CreateToolPlanRequest> plans,
	@Schema(description = "블로그 링크 목록")
	List<String> blogLinks,
	@Schema(description = "툴 소개 이미지 링크 목록")
	List<String> images,
	@Schema(description = "참고하면 좋은 영상 링크 목록")
	List<String> videos,
	@Schema(description = "관련 툴 ID 목록")
	List<Long> relatedToolIds
) {
}
