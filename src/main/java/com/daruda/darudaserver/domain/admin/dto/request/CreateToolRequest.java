package com.daruda.darudaserver.domain.admin.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateToolRequest(
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
	boolean supportKorea,
	@Schema(description = "소개 글")
	@Size(max = 500, message = "소개 글은 500자 이내로 작성해주세요.")
	String detailDescription,
	@Schema(description = "가격정책 링크")
	String planLink,
	@Schema(description = "로고 이미지 링크")
	String toolLogo,
	@Schema(description = "플랫폼 지원 정보")
	CreateToolPlatFormRequest toolPlatForm,
	@Schema(description = "키워드 목록")
	List<String> keywords,
	@Schema(description = "핵심 기능 목록")
	List<CreateToolCoreRequest> cores,
	@Schema(
		description = "플랜 유형",
		example = "무료",
		allowableValues = {"무료", "월간", "구매", "월간 & 연간"},
		requiredMode = Schema.RequiredMode.NOT_REQUIRED
	)
	String planType,
	@Schema(
		description = """
				플랜 목록 (배열)

				**사용 가이드:**
				- planType에 따라 플랜 구조가 달라집니다
				- 무료: 빈 배열 또는 무료 플랜 1개
				- 월간: 여러 월간 구독 플랜 (예: Basic, Pro, Enterprise)
				- 구매: 여러 일회성 구매 플랜 (예: Starter, Professional)
				- 월간 & 연간: 월간과 연간 플랜 혼합

				**예시:**
			- 무료: [] 또는 [{"planName":"무료", "planPrice":0, "planDescription":"기본 기능 제공"}]
			- 월간: [{"planName":"Basic", "planPrice":10000, "planDescription":"기본 기능"},
				{"planName":"Pro", "planPrice":30000, "planDescription":"고급 기능}]
			- 구매: [{"planName":"Starter", "planPrice":50000, "planDescription":"시작 패키지"},
				{"planName":"Professional", "planPrice":150000, "planDescription":"전문가 패키지"}]
				**주의:** 각 플랜의 planName, planPrice, planDescription은 모두 필수입니다.
			""",
		requiredMode = Schema.RequiredMode.NOT_REQUIRED
	)
	@Valid
	List<CreateToolPlanRequest> plans,
	@Schema(description = "블로그 링크 목록")
	List<String> blogLinks,
	@Schema(description = "툴 소개 이미지 링크 목록")
	List<String> images,
	@Schema(description = "참고하면 좋은 영상 링크 목록")
	List<String> videos,
	@Schema(description = "관련 툴 ID 목록")
	List<Integer> relatedToolIds
) {
}
