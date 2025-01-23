package com.daruda.darudaserver.domain.tool.service;


import com.daruda.darudaserver.domain.tool.dto.res.*;
import com.daruda.darudaserver.domain.tool.entity.*;
import com.daruda.darudaserver.domain.tool.repository.*;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.common.response.ScrollPaginationDto;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

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
    private final JPAQueryFactory jpaQueryFactory;

    QTool qTool = QTool.tool;

    public ToolDetailGetRes getToolDetail(final Long userIdOrNull, final Long toolId) {
        log.info("툴 세부 정보를 조회합니다. toolId={}" + userIdOrNull);

        Tool tool = getToolById(toolId);
        List<String> images = getImageById(tool);
        List<PlatformRes> platformRes = convertToPlatformRes(tool);
        List<String> keywordRes = convertToKeywordRes(tool);
        List<String> videos = getVideoById(tool);
        tool.incrementViewCount();

        UserEntity user;
        Boolean isScrapped = false;
        //AccessToken 이 들어왔을 경우
        if (userIdOrNull != null) {
            Long userId = userIdOrNull;
            user = userRepository.findById(userId)
                    .orElse(null);
            log.debug("유저 정보를 조회했습니다: {}", user.getId());
            isScrapped = getScrapped(user, tool);
        }
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

    public ToolListRes getToolList(final Long userIdOrNull, final String criteria, final String category, final int size, final Long lastToolId, final Boolean isFree) {
        log.debug("카테고리별 툴 목록을 조회 category: {}, sort: {}, size: {}, lastToolId: {}", category, criteria, size, lastToolId);

        UserEntity user;
        if (userIdOrNull != null) {
            Long userId = userIdOrNull;
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
            log.debug("유저 정보를 조회했습니다: {}", user.getId());
        } else {
            user = null;
        }
        // lastToolId 가 null 일 경우 에러 발생
        Long lastSortValue = getLastSortValue(lastToolId, criteria);

        List<Tool> tools = jpaQueryFactory
                .selectFrom(qTool)
                .where(
                        categoryEq(category),
                        isFreeEq(isFree),
                        cursorCondition(lastToolId, lastSortValue, criteria) // Cursor 페이징 조건
                )
                .orderBy(getSortOrder(criteria))
                .limit(size + 1)
                .fetch();

        validateCriteria(criteria);


        long totalElements = getTotalElements(category,isFree);
        long nextCursor = getNextCursor(tools, size);


        boolean hasNextPage = tools.size() > size;
        List<Tool> paginatedTools = hasNextPage ? tools.subList(0, size) : tools;
        List<ToolResponse> toolResponses = paginatedTools.stream()
                .map(tool -> {
                    boolean isScraped = (
                            toolScrapRepository.findByUserAndTool(user, tool)
                                    .map(toolScrap -> !toolScrap.isDelYn())
                                    .orElse(false));
                    log.debug("스크랩 여부" + tool.getToolId() + isScraped);
                    return ToolResponse.of(tool, convertToKeywordRes(tool), isScraped);
                })
                .toList();

        ScrollPaginationDto scrollPaginationDto = ScrollPaginationDto.of(totalElements, nextCursor);
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

    // 정렬 기준 검증
    public void validateCriteria(String criteria) {
        List<String> allowedFields = List.of("popular", "createdAt");
        if (!allowedFields.contains(criteria)) {
            throw new IllegalArgumentException("Invalid sort criteria: " + criteria);
        }
    }

    // 카테고리 필터링
    private BooleanExpression categoryEq(String category) {
        return (category == null || category.equals("ALL")) ? null : qTool.category.eq(Category.valueOf(category));
    }

    // 무료 여부 필터링
    private BooleanExpression isFreeEq(Boolean isFree) {
        if (isFree == null) return null; // 필터링 없음
        if (!isFree) return null; // false이면 모든 데이터
        return qTool.license.eq(License.FREE); // true이면 무료 데이터만
    }

    private BooleanExpression cursorCondition(Long lastToolId, Long lastSortValue, String criteria) {
        if (lastSortValue == null || lastToolId == null) return null;  // 첫 페이지일 경우 null 반환

        if ("popular".equals(criteria)) {
            return qTool.popular.lt(lastSortValue.intValue())
                    .or(qTool.popular.eq(lastSortValue.intValue()).and(qTool.toolId.lt(lastToolId)));
        } else {
            return qTool.createdAt.lt(new Timestamp(lastSortValue))
                    .or(qTool.createdAt.eq(new Timestamp(lastSortValue)).and(qTool.toolId.lt(lastToolId)));
        }
    }


    private OrderSpecifier<?>[] getSortOrder(String criteria) {
        if ("popular".equals(criteria)) {
            return new OrderSpecifier<?>[]{
                    qTool.popular.desc().nullsLast(),
                    qTool.toolId.desc()
            };
        } else {
            return new OrderSpecifier<?>[]{
                    qTool.createdAt.desc().nullsLast(),
                    qTool.toolId.desc()
            };
        }
    }

    private Long getLastSortValue(final Long lastToolId, String criteria) {
        if (lastToolId == null) {
            return "popular".equals(criteria) ? Long.MAX_VALUE : System.currentTimeMillis();
        }
        Tool tool = getToolById(lastToolId+1);
        return "popular".equals(criteria) ? tool.getPopular() : tool.getCreatedAt().getTime();
    }

    private Long getNextCursor(List<Tool> tools, int size) {
        if (tools.size() > size) {
            return tools.get(size).getToolId();
        } else if (!tools.isEmpty()) {
            Long lastToolId = tools.get(tools.size() - 1).getToolId();
            if (lastToolId.equals(tools.get(tools.size() - 1).getToolId())) {
                return -1L;
            }

            return lastToolId;
        }
        return -1L;
    }

    private long getTotalElements(String category, Boolean isFree) {
        BooleanExpression categoryCondition = categoryEq(category);
        BooleanExpression freeCondition = isFreeEq(isFree);

        return Optional.ofNullable(jpaQueryFactory
                        .select(qTool.count())
                        .from(qTool)
                        .where(
                                categoryCondition,
                                freeCondition
                        )
                        .fetchOne())
                .orElse(0L);

    }
}
