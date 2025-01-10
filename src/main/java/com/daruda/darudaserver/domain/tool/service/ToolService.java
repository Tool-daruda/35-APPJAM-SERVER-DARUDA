package com.daruda.darudaserver.domain.tool.service;


import com.daruda.darudaserver.domain.tool.dto.res.*;
import com.daruda.darudaserver.domain.tool.entity.*;
import com.daruda.darudaserver.domain.tool.repository.*;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
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


    public ToolDetailGetRes getToolDetail(final Long toolId){
        Tool tool = getToolById(toolId);
        List<String> images = getImageById(tool);
        List<PlatformRes> platformRes = convertToPlatformRes(tool);
        List<String> keywordRes = convertToKeywordRes(tool);
        List<String> videos = getVideoById(tool);
        return ToolDetailGetRes.of(tool,platformRes,keywordRes, images,videos);
    }

    public PlanRes getPlan(final Long toolId){
        Tool tool = getToolById(toolId);
        Plan plan = getPlanByTool(tool);
        return PlanRes.of(plan);
    }
    public ToolCoreRes getToolCore(final Long toolId){
        Tool tool = getToolById(toolId);
        ToolCore toolCore = getToolCoreByTool(tool);
        return ToolCoreRes.of(toolCore);
    }

    public RelatedToolListRes getRelatedTool(final Long toolId){
        Tool tool = getToolById(toolId);
        List<RelatedTool> relatedTools = relatedTool(tool);
        List<RelatedToolRes> relatedToolResList = relatedTools.stream()
                .map(relatedTool -> {
                    Tool related = relatedTool.getAlternativeTool();
                    List<String> keywords = convertToKeywordRes(related);
                    return RelatedToolRes.of(related, keywords);
                })
                .toList();
        return RelatedToolListRes.of(relatedToolResList);
    }

    private List<RelatedTool> relatedTool(final Tool tool){
        return relatedToolRepository.findAllByTool(tool);
    }

    private Tool getToolById(final Long toolId){
            return toolRepository.findById(toolId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND));
    }

    private Plan getPlanByTool(final Tool tool){
        return planRepository.findByTool(tool)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND));
    }

    private ToolCore getToolCoreByTool(final Tool tool){
        return toolCoreRepository.findByTool(tool)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND));
    }



    private List<String> getImageById(final Tool tool){
        List<ToolImage> toolImages = toolImageRepository.findAllByTool(tool);
        return toolImages.stream()
                .map(ToolImage::getImageUrl)
                .toList();
    }

    private List<String> getVideoById(final Tool tool){
        List<ToolVideo> toolVideos = toolVideoRepository.findAllByTool(tool);
        return toolVideos.stream()
                .map(ToolVideo::getVideoUrl)
                .toList();
    }

    private List<PlatformRes> convertToPlatformRes(Tool tool) {
        List<ToolPlatForm> toolPlatForms = toolPlatFormRepository.findAllByTool(tool);
         return toolPlatForms.stream()
                .map(PlatformRes::of)
                .toList();
    }

    private List<String> convertToKeywordRes(Tool tool) {
        List<ToolKeyword> toolKeywords = toolKeywordRepository.findAllByTool(tool);
        return toolKeywords.stream()
                .map(ToolKeyword::getKeywordName)
                .toList();
    }
}
