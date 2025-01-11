package com.daruda.darudaserver.domain.tool.service;


import com.daruda.darudaserver.domain.tool.dto.res.*;
import com.daruda.darudaserver.domain.tool.entity.*;
import com.daruda.darudaserver.domain.tool.repository.*;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


    public ToolDetailGetRes getToolDetail(final Long toolId) {
        log.info("툴 세부 정보를 조회합니다. toolId={}", toolId);

        Tool tool = getToolById(toolId);
        List<String> images = getImageById(tool);
        List<PlatformRes> platformRes = convertToPlatformRes(tool);
        List<String> keywordRes = convertToKeywordRes(tool);
        List<String> videos = getVideoById(tool);
        updateView(toolId);

        log.info("툴 세부 정보를 성공적으로 조회했습니다. toolId={}", toolId);
        return ToolDetailGetRes.of(tool, platformRes, keywordRes, images, videos);
    }

    public PlanRes getPlan(final Long toolId) {
        log.info("플랜 정보를 조회합니다. toolId={}", toolId);

        Tool tool = getToolById(toolId);
        Plan plan = getPlanByTool(tool);

        log.info("플랜 정보를 성공적으로 조회했습니다. toolId={}", toolId);
        return PlanRes.of(plan);
    }

    public ToolCoreRes getToolCore(final Long toolId) {
        log.info("툴 핵심 정보를 조회합니다. toolId={}", toolId);

        Tool tool = getToolById(toolId);
        ToolCore toolCore = getToolCoreByTool(tool);

        log.info("툴 핵심 정보를 성공적으로 조회했습니다. toolId={}", toolId);
        return ToolCoreRes.of(toolCore);
    }

    public RelatedToolListRes getRelatedTool(final Long toolId) {
        log.info("관련 툴 정보를 조회합니다. toolId={}", toolId);

        Tool tool = getToolById(toolId);
        List<RelatedTool> relatedTools = relatedTool(tool);

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

    private List<RelatedTool> relatedTool(final Tool tool) {
        log.debug("툴의 관련 툴 데이터를 조회합니다. toolId={}", tool.getToolId());
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

    private Plan getPlanByTool(final Tool tool) {
        log.debug("툴에 연결된 플랜 정보를 조회합니다. toolId={}", tool.getToolId());
        return planRepository.findByTool(tool)
                .orElseThrow(() -> {
                    log.error("플랜 정보를 찾을 수 없습니다. toolId={}", tool.getToolId());
                    return new NotFoundException(ErrorCode.DATA_NOT_FOUND);
                });
    }

    private ToolCore getToolCoreByTool(final Tool tool) {
        log.debug("툴에 연결된 핵심 정보를 조회합니다. toolId={}", tool.getToolId());
        return toolCoreRepository.findByTool(tool)
                .orElseThrow(() -> {
                    log.error("핵심 정보를 찾을 수 없습니다. toolId={}", tool.getToolId());
                    return new NotFoundException(ErrorCode.DATA_NOT_FOUND);
                });
    }

    private List<String> getImageById(final Tool tool) {
        List<ToolImage> toolImages = toolImageRepository.findAllByTool(tool);
        log.debug("툴에 연결된 플랫폼 정보를 조회했습니다");
        return toolImages.stream()
                .map(ToolImage::getImageUrl)
                .toList();
    }

    private List<String> getVideoById(final Tool tool) {
        List<ToolVideo> toolVideos = toolVideoRepository.findAllByTool(tool);
        log.debug("툴에 연결된 비디오 목록을 조회했습니다");
        return toolVideos.stream()
                .map(ToolVideo::getVideoUrl)
                .toList();
    }

    private List<PlatformRes> convertToPlatformRes(Tool tool) {
        List<ToolPlatForm> toolPlatForms = toolPlatFormRepository.findAllByTool(tool);
        log.debug("툴에 연결된 플랫폼 정보를 조회했습니다");
        return toolPlatForms.stream()
                .map(PlatformRes::of)
                .toList();
    }

    private List<String> convertToKeywordRes(Tool tool) {
        log.debug("툴에 연결된 키워드 정보를 조회했습니다");
        List<ToolKeyword> toolKeywords = toolKeywordRepository.findAllByTool(tool);
        return toolKeywords.stream()
                .map(ToolKeyword::getKeywordName)
                .toList();
    }

    public int updateView(Long toolId) {
        log.debug("툴 조회수를 증가시킵니다. toolId={}", toolId);
        return toolRepository.updateView(toolId);
    }
}
