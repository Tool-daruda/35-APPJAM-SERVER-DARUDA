package com.daruda.darudaserver.domain.admin.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
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
	@Schema(description = "로고 이미지 링크")
	String toolLogo,
	@Schema(description = "플랫폼 지원 정보")
	@Nullable CreateToolPlatFormRequest toolPlatForm,
	@Schema(description = "키워드 목록")
	List<String> keywords,
	@Schema(description = "핵심 기능 목록")
	List<CreateToolCoreRequest> cores,
	@Schema(
		description = "플랜 유형",
		example = "월간",
		allowableValues = {"무료", "월간", "구매", "월간 & 연간"},
		requiredMode = Schema.RequiredMode.NOT_REQUIRED
	)
	String planType,
	@Schema(
		description = """
			플랜 목록 (배열)

			**수정 동작:**
			- 이 필드를 전송하면 기존 플랜들이 모두 삭제되고 새로운 플랜들로 교체됩니다
			- null로 보내면 플랜 정보는 변경되지 않습니다
			- 빈 배열([])로 보내면 모든 플랜이 삭제됩니다

			**주의사항:**
			- 플랜을 수정할 때는 유지할 플랜도 포함하여 전체 플랜 목록을 다시 전송해야 합니다
			- planName, planPrice, planDescription이 null인 플랜은 저장되지 않습니다 (모두 필수)
			- planDescription은 최대 500자까지 입력 가능합니다

			**예시:**
			- 플랜 추가: [{"planName":"New Plan", "planPrice":20000, "planDescription":"새로운 플랜"}]
			- 플랜 삭제: [] (모든 플랜 삭제)
			- 플랜 유지: null (기존 플랜 그대로 유지)
			- 플랜 수정: [수정된 전체 플랜 목록]
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
	List<Long> relatedToolIds
) {
}
