package com.daruda.darudaserver.domain.tool.service;


import com.daruda.darudaserver.domain.tool.dto.res.*;
import com.daruda.darudaserver.domain.tool.entity.*;
import com.daruda.darudaserver.domain.tool.repository.*;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public ToolDetailGetRes getToolDetail(final Long toolId) {
        log.info("툴 세부 정보를 조회합니다. toolId={}", toolId);

        Tool tool = getToolById(toolId);
        List<String> images = getImageById(tool);
        List<PlatformRes> platformRes = convertToPlatformRes(tool);
        List<String> keywordRes = convertToKeywordRes(tool);
        List<String> videos = getVideoById(tool);
        tool.incrementViewCount();
        log.debug("툴의 조회수가 증가되었습니다" + tool.getViewCount());
        log.info("툴 세부 정보를 성공적으로 조회했습니다. toolId={}", toolId);
        toolRepository.save(tool);
        return ToolDetailGetRes.of(tool, platformRes, keywordRes, images, videos);
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

    public ToolListRes getToolList(final String criteria, final Category category, final int size, final Long lastToolId) {
        log.debug("카테고리별 툴 목록을 조회 category: {}, sort: {}, size: {}, lastToolId: {}", category, criteria, size, lastToolId);

        Long cursor = (lastToolId == null) ? Long.MAX_VALUE : lastToolId;
        validateCriteria(criteria);
        Pageable pageRequest = PageRequest.of(0, size + 1);  // size + 1 조회하여 다음 페이지 존재 여부 확인

        List<Tool> tools;
        long totalElements = Category.ALL.equals(category)
                ? toolRepository.count()
                : toolRepository.countWithCursor(category, cursor);

        if ("popular".equals(criteria)) {
            tools = Category.ALL.equals(category)
                    ? toolRepository.findAllWithCursorOrderByPopular(cursor, pageRequest)
                    : toolRepository.findByCategoryWithCursorOrderByPopular(category, cursor, pageRequest);
        } else {
            tools = Category.ALL.equals(category)
                    ? toolRepository.findAllWithCursorOrderByCreatedAt(cursor, pageRequest)
                    : toolRepository.findByCategoryWithCursorOrderByCreatedAt(category, cursor, pageRequest);
        }

        // nextCursor : size + 1로 조회했을 때 다음 데이터가 있는지 확인
        boolean hasNextPage = tools.size() > size;
        List<Tool> paginatedTools = hasNextPage ? tools.subList(0, size) : tools;
        long nextCursor = hasNextPage ? tools.get(size).getToolId() : -1; // 다음 페이지가 있으면 cursor 설정
        ScrollPaginationDto scrollPaginationDto = ScrollPaginationDto.of(totalElements, nextCursor);
        List<ToolResponse> toolResponses = paginatedTools.stream()
                .map(tool -> ToolResponse.of(tool, convertToKeywordRes(tool)))
                .toList();

        return ToolListRes.of(toolResponses, scrollPaginationDto);
    }


    @Transactional
    public ToolScrapRes postToolScrap(final Long userId, final Long toolId){
        UserEntity user = getUserById(userId);
        Tool tool = getToolById(toolId);
        ToolScrap toolScrap = toolScrapRepository.findByUserAndTool(user, tool).orElse(null);

        if(toolScrap==null){
            toolScrap = ToolScrap.builder()
                    .user(user)
                    .tool(tool)
                    .build();
            toolScrapRepository.save(toolScrap);
            log.debug("툴 스크랩이 생 되었습니다");
        }else{
            log.debug("툴 스크랩이 업데이트 되었습니다");
            toolScrap.update();
        }
        int scrapCount = toolScrapRepository.countByTool_ToolIdAndDelYnFalse(toolId);
        tool.updatePopular(scrapCount);
        return ToolScrapRes.of(toolId, toolScrap.isDelYn());
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
        validateList(toolVideos);
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
        // 빈 리스트일 경우 예외 처리
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

    // 정렬 기준 검증
    public void validateCriteria(String criteria){
        List<String> allowedFields = List.of("popular", "createdAt");
        if (!allowedFields.contains(criteria)) {
            throw new IllegalArgumentException("Invalid sort criteria: " + criteria);
        }
    }
}
