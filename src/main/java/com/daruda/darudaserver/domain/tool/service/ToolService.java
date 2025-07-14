package com.daruda.darudaserver.domain.tool.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.tool.dto.res.PlanListRes;
import com.daruda.darudaserver.domain.tool.dto.res.PlanRes;
import com.daruda.darudaserver.domain.tool.dto.res.PlatformRes;
import com.daruda.darudaserver.domain.tool.dto.res.RelatedToolListRes;
import com.daruda.darudaserver.domain.tool.dto.res.RelatedToolRes;
import com.daruda.darudaserver.domain.tool.dto.res.ToolCoreListRes;
import com.daruda.darudaserver.domain.tool.dto.res.ToolCoreRes;
import com.daruda.darudaserver.domain.tool.dto.res.ToolDetailGetRes;
import com.daruda.darudaserver.domain.tool.dto.res.ToolListRes;
import com.daruda.darudaserver.domain.tool.dto.res.ToolResponse;
import com.daruda.darudaserver.domain.tool.dto.res.ToolScrapRes;
import com.daruda.darudaserver.domain.tool.entity.License;
import com.daruda.darudaserver.domain.tool.entity.Plan;
import com.daruda.darudaserver.domain.tool.entity.RelatedTool;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolCore;
import com.daruda.darudaserver.domain.tool.entity.ToolImage;
import com.daruda.darudaserver.domain.tool.entity.ToolKeyword;
import com.daruda.darudaserver.domain.tool.entity.ToolPlatForm;
import com.daruda.darudaserver.domain.tool.entity.ToolScrap;
import com.daruda.darudaserver.domain.tool.entity.ToolVideo;
import com.daruda.darudaserver.domain.tool.repository.PlanRepository;
import com.daruda.darudaserver.domain.tool.repository.RelatedToolRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolCoreRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolImageRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolKeywordRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolPlatFormRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolScrapRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolVideoRepository;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class ToolService {

	private final ToolRepository toolRepository;
	private final ToolImageRepository toolImageRepository;
	private final ToolPlatFormRepository toolPlatFormRepository;
	private final ToolVideoRepository toolVideoRepository;
	private final ToolKeywordRepository toolKeywordRepository;
	private final PlanRepository planRepository;
	private final ToolCoreRepository toolCoreRepository;
	private final RelatedToolRepository relatedToolRepository;
	private final ToolScrapRepository toolScrapRepository;
	private final UserRepository userRepository;

	public ToolDetailGetRes getToolDetail(Long userId, final Long toolId) {
		log.info("툴 세부 정보를 조회합니다. toolId={}, userId={}", toolId, userId);

		Tool tool = getToolById(toolId);
		List<String> images = getImageById(tool);
		List<PlatformRes> platformRes = convertToPlatformRes(tool);
		List<String> keywordRes = convertToKeywordRes(tool);
		List<String> videos = getVideoById(tool);
		tool.incrementViewCount();

		Boolean isScrapped = Optional.ofNullable(userId)
			.flatMap(userRepository::findById)
			.map(user -> {
				log.debug("유저 정보를 조회했습니다: {}", user.getId());
				return getScrapped(user, tool);
			})
			.orElse(false);

		log.debug("툴의 조회수가 증가되었습니다" + tool.getViewCount());
		log.info("툴 세부 정보를 성공적으로 조회했습니다. toolId={}", toolId);
		toolRepository.save(tool);
		return ToolDetailGetRes.of(tool, platformRes, tool.getToolLogo(), keywordRes, images, videos, isScrapped);
	}

	public PlanListRes getPlan(final Long toolId) {
		log.info("플랜 정보를 조회합니다. toolId={}", toolId);
		Tool tool = getToolById(toolId);
		List<PlanRes> plan = getPlanByTool(tool);
		log.info("플랜 정보를 성공적으로 조회했습니다. toolId={}", toolId);
		return PlanListRes.of(plan);
	}

	public ToolCoreListRes getToolCore(final Long toolId) {
		log.debug("툴 핵심 정보를 조회합니다. toolId={}", toolId);
		Tool tool = getToolById(toolId);
		List<ToolCoreRes> toolCore = getToolCoreByTool(tool);
		log.info("툴 핵심 정보를 성공적으로 조회했습니다. toolId={}", toolId);
		return ToolCoreListRes.of(toolCore);
	}

	public RelatedToolListRes getRelatedTool(final Long toolId) {
		log.info("관련 툴 정보를 조회합니다. toolId={}", toolId);
		Tool tool = getToolById(toolId);
		List<RelatedTool> relatedTools = relatedTool(tool);
		validateList(relatedTools);
		List<RelatedToolRes> relatedToolResList = relatedTools.stream()
			.map(relatedTool -> {
				Tool related = relatedTool.getAlternativeTool();
				List<String> keywords = convertToKeywordRes(related);
				return RelatedToolRes.of(related, keywords);
			})
			.toList();

		log.info("관련 툴 정보를 성공적으로 조회했습니다. toolId={}", toolId);
		return RelatedToolListRes.of(relatedToolResList);
	}

	public ToolListRes getToolList(final Long userIdOrNull, final String criteria, final String category,
		final int size, final Long lastToolId, final Boolean isFree) {
		log.debug("카테고리별 툴 목록을 조회 category: {}, sort: {}, size: {}, lastToolId: {}", category, criteria, size,
			lastToolId);

		UserEntity user;
		if (userIdOrNull != null) {
			user = userRepository.findById(userIdOrNull)
				.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
			log.debug("유저 정보를 조회했습니다: {}", user.getId());
		} else {
			user = null;
		}

		//  1. 모든 데이터 한 번에 가져오기
		List<Tool> allTools = toolRepository.findAll();
		log.debug(" 전체 데이터 개수: {}", allTools.size());

		//  2. 필터링 (카테고리 & 무료 여부)
		List<Tool> filteredTools = allTools.stream()
			.filter(tool -> (category == null || category.equals("ALL") || tool.getCategory().name().equals(category)))
			.filter(tool -> (isFree == null || (isFree && tool.getLicense() == License.FREE) || (!isFree)))
			.toList();

		log.debug("필터링 후 데이터 개수: {}", filteredTools.size());

		//  3. 정렬 (인기순 또는 등록순)
		if ("popular".equals(criteria)) {
			filteredTools = filteredTools.stream()
				.sorted((t1, t2) -> {
					int cmp = Integer.compare(t2.getPopular(), t1.getPopular());
					return cmp != 0 ? cmp : Long.compare(t2.getToolId(), t1.getToolId());
				})
				.toList();
		} else {
			filteredTools = filteredTools.stream()
				.sorted((t1, t2) -> {
					int cmp = t2.getCreatedAt().compareTo(t1.getCreatedAt());
					return cmp != 0 ? cmp : Long.compare(t2.getToolId(), t1.getToolId());
				})
				.toList();
		}

		log.debug("정렬 후 데이터 개수 : " + filteredTools.size());

		// `lastToolId` 포함하여 이후 데이터 가져오기
		int startIndex = 0;
		if (lastToolId != null) {
			Optional<Tool> lastTool = filteredTools.stream()
				.filter(tool -> tool.getToolId().equals(lastToolId))
				.findFirst();

			if (lastTool.isPresent()) {
				startIndex = filteredTools.indexOf(lastTool.get());  //`lastToolId`부터 포함하여 시작
			} else {
				log.warn(" lastToolId({})에 해당하는 데이터가 존재하지 않음. 처음부터 시작", lastToolId);
			}
		}

		List<Tool> paginatedTools = filteredTools.subList(startIndex,
			Math.min(startIndex + size, filteredTools.size()));

		//  nextCursor 설정 (현재 응답에서 마지막 `toolId` 다음에 나올 `toolId`)
		Long nextCursor = -1L;
		int lastIndex = startIndex + paginatedTools.size(); // 현재 페이지의 마지막 요소 인덱스

		if (lastIndex < filteredTools.size()) {
			nextCursor = filteredTools.get(lastIndex).getToolId(); // 다음 `toolId` 설정
		}

		//  응답 데이터 변환
		List<ToolResponse> toolResponses = paginatedTools.stream()
			.map(tool -> {
				boolean isScraped = user != null && toolScrapRepository.findByUserAndTool(user, tool)
					.map(toolScrap -> !toolScrap.isDelYn())
					.orElse(false);
				return ToolResponse.of(tool, convertToKeywordRes(tool), isScraped);
			})
			.toList();

		ScrollPaginationDto scrollPaginationDto = ScrollPaginationDto.of(filteredTools.size(), nextCursor);
		return ToolListRes.of(toolResponses, scrollPaginationDto);
	}

	public ToolScrapRes postToolScrap(final Long userId, final Long toolId) {
		UserEntity user = getUserById(userId);
		Tool tool = getToolById(toolId);
		ToolScrap toolScrap = toolScrapRepository.findByUserAndTool(user, tool).orElse(null);

		if (toolScrap == null) {
			toolScrap = ToolScrap.builder()
				.user(user)
				.tool(tool)
				.build();
			toolScrapRepository.save(toolScrap);
			log.debug("툴 스크랩이 생 되었습니다");
		} else {
			log.debug("툴 스크랩이 업데이트 되었습니다");
			toolScrap.update();
		}
		int scrapCount = toolScrapRepository.countByTool_ToolIdAndDelYnFalse(toolId);
		tool.updatePopular(scrapCount);
		return ToolScrapRes.of(toolId, !toolScrap.isDelYn());
	}

	private List<RelatedTool> relatedTool(final Tool tool) {
		log.info("툴의 관련 툴 데이터를 조회합니다. toolId={}", tool.getToolId());
		return relatedToolRepository.findAllByTool(tool);
	}

	private Tool getToolById(final Long toolId) {
		log.debug("툴을 조회합니다. toolId={}", toolId);
		return toolRepository.findById(toolId)
			.orElseThrow(() -> {
				log.error("툴을 찾을 수 없습니다. toolId={}", toolId);
				return new NotFoundException(ErrorCode.DATA_NOT_FOUND);
			});
	}

	private List<PlanRes> getPlanByTool(final Tool tool) {
		log.debug("툴에 연결된 플랜 정보를 조회합니다. toolId={}", tool.getToolId());
		List<Plan> planList = planRepository.findAllByTool(tool);
		validateList(planList);
		return planList.stream()
			.map(PlanRes::of)
			.toList();

	}

	private List<ToolCoreRes> getToolCoreByTool(final Tool tool) {
		log.debug("툴에 연결된 핵심 정보를 조회합니다. toolId={}", tool.getToolId());
		List<ToolCore> toolCoreList = toolCoreRepository.findAllByTool(tool);
		validateList(toolCoreList);
		return toolCoreList.stream()
			.map(ToolCoreRes::of)
			.toList();
	}

	private List<String> getImageById(final Tool tool) {
		List<ToolImage> toolImages = toolImageRepository.findAllByTool(tool);
		validateList(toolImages);
		log.debug("툴에 연결된 플랫폼 정보를 조회했습니다");
		return toolImages.stream()
			.map(ToolImage::getImageUrl)
			.toList();
	}

	private List<String> getVideoById(final Tool tool) {
		List<ToolVideo> toolVideos = toolVideoRepository.findAllByTool(tool);
		//validateList(toolVideos);
		log.debug("툴에 연결된 비디오 목록을 조회했습니다");
		return toolVideos.stream()
			.map(ToolVideo::getVideoUrl)
			.toList();
	}

	private List<PlatformRes> convertToPlatformRes(Tool tool) {
		List<ToolPlatForm> toolPlatForms = toolPlatFormRepository.findAllByTool(tool);
		validateList(toolPlatForms);
		log.debug("툴에 연결된 플랫폼 정보를 조회했습니다");
		return toolPlatForms.stream()
			.map(PlatformRes::of)
			.toList();
	}

	public List<String> convertToKeywordRes(Tool tool) {
		List<ToolKeyword> toolKeywords = toolKeywordRepository.findAllByTool(tool);
		validateList(toolKeywords);
		log.debug("툴에 연결된 키워드 정보를 조회했습니다");
		return toolKeywords.stream()
			.map(ToolKeyword::getKeywordName)
			.toList();
	}

	private void validateList(List<?> lists) {
		if (lists == null || lists.isEmpty()) {
			throw new NotFoundException(ErrorCode.DATA_NOT_FOUND);
		}
	}

	public UserEntity getUserById(final Long userId) {
		log.debug("유저를 조회합니다. userId={}", userId);
		return userRepository.findById(userId)
			.orElseThrow(() -> {
				log.error("유저를 찾을 수 없습니다. userId={}", userId);
				return new NotFoundException(ErrorCode.DATA_NOT_FOUND);
			});
	}

	public Boolean getScrapped(final UserEntity user, final Tool tool) {
		ToolScrap toolScrap = toolScrapRepository.findByUserAndTool(user, tool)
			.orElse(null);
		if (toolScrap == null) {
			return false;
		}
		return !toolScrap.isDelYn();
	}

	public List<String> getKeywords(final Long toolId) {
		Tool tool = getToolById(toolId);
		return convertToKeywordRes(tool);
	}
}
