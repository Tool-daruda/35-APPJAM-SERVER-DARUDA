package com.daruda.darudaserver.domain.tool.controller;

import com.daruda.darudaserver.domain.tool.dto.res.*;
import com.daruda.darudaserver.domain.tool.service.ToolService;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tools")
public class ToolController {

    private final ToolService toolService;

    /**
     * 툴 세부정보 조회
     */
    @GetMapping("/{tool-id}")
    public ResponseEntity<ApiResponse<?>> getToolDetail(@PathVariable(name="tool-id") final Long toolId){
        ToolDetailGetRes toolDetail = toolService.getToolDetail(toolId);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(toolDetail, SuccessCode.SUCCESS_FETCH));
    }

    /**
     * 툴 핵심 기능 조회
     */
    @GetMapping("/{tool-id}/core-features")
    public ResponseEntity<ApiResponse<?>> getToolCoreFeature(@PathVariable(name="tool-id") final Long toolId){
        ToolCoreListRes toolCore = toolService.getToolCore(toolId);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(toolCore, SuccessCode.SUCCESS_FETCH));
    }

    /**
     * 툴- 플랜 조회
     */
    @GetMapping("/{tool-id}/plans")
    public ResponseEntity<ApiResponse<?>> getToolPlans(@PathVariable(name="tool-id") final Long toolId){
        PlanListRes plan = toolService.getPlan(toolId);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(plan, SuccessCode.SUCCESS_FETCH));
    }

    /**
     * 대안툴 조회
     */
    @GetMapping("/{tool-id}/related-tool")
    public ResponseEntity<ApiResponse<?>> getRelatedTool(@PathVariable(name="tool-id") final Long toolId){
        RelatedToolListRes relatedTool = toolService.getRelatedTool(toolId);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(relatedTool, SuccessCode.SUCCESS_FETCH));
    }

}
