package com.daruda.darudaserver.domain.tool.controller;

import com.daruda.darudaserver.domain.tool.dto.res.*;
import com.daruda.darudaserver.domain.tool.entity.Category;
import com.daruda.darudaserver.domain.tool.service.ToolService;
import com.daruda.darudaserver.global.auth.UserId;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.SuccessCode;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ToolController {

    private final ToolService toolService;

    /**
     * 툴 세부정보 조회
     */
    @GetMapping("/tools/{tool-id}")
    public ResponseEntity<ApiResponse<?>> getToolDetail(@PathVariable(name="tool-id") final Long toolId){
        ToolDetailGetRes toolDetail = toolService.getToolDetail(toolId);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(toolDetail, SuccessCode.SUCCESS_FETCH));
    }

    /**
     * 툴 핵심 기능 조회
     */
    @GetMapping("/tools/{tool-id}/core-features")
    public ResponseEntity<ApiResponse<?>> getToolCoreFeature(@PathVariable(name="tool-id") final Long toolId){
        ToolCoreListRes toolCore = toolService.getToolCore(toolId);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(toolCore, SuccessCode.SUCCESS_FETCH));
    }

    /**
     * 툴- 플랜 조회
     */
    @GetMapping("/tools/{tool-id}/plans")
    public ResponseEntity<ApiResponse<?>> getToolPlans(@PathVariable(name="tool-id") final Long toolId){
        PlanListRes plan = toolService.getPlan(toolId);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(plan, SuccessCode.SUCCESS_FETCH));
    }

    /**
     * 대안툴 조회
     */
    @GetMapping("/tools/{tool-id}/related-tool")
    public ResponseEntity<ApiResponse<?>> getRelatedTool(@PathVariable(name="tool-id") final Long toolId){
        RelatedToolListRes relatedTool = toolService.getRelatedTool(toolId);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(relatedTool, SuccessCode.SUCCESS_FETCH));
    }

    /**
     * 툴 리스트 조회
     */
    @GetMapping("/tools")
    public ResponseEntity<ApiResponse<?>> getToolList(@RequestParam(defaultValue = "인기순") String sort,
                                                      @RequestParam(defaultValue = "전체") String category,
                                                      @RequestParam(value = "size", defaultValue = "10") int size,
                                                      @RequestParam(value = "lastBoardId", required = false) Long lastBoardId
                                                             ){
        Category categoryEnum = Category.fromKoreanName(category);
        ToolListRes toolListRes = toolService.
                getToolList(sort , categoryEnum, size, lastBoardId);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(toolListRes,SuccessCode.SUCCESS_FETCH));
    }
    /**
     * 툴 찜하기
     */
    @PostMapping("/users/tools/{tool-id}/scrap")
    public ResponseEntity<ApiResponse<?>> postToolScrap(@UserId final Long userId, @PathVariable(name="tool-id") final Long toolId){
        ToolScrapRes toolScrapRes = toolService.postToolScrap(userId, toolId);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(toolScrapRes, SuccessCode.SUCCESS_CREATE));
    }
}
