package com.daruda.darudaserver.domain.tool.controller;

import com.daruda.darudaserver.domain.tool.dto.res.*;
import com.daruda.darudaserver.domain.tool.entity.Category;
import com.daruda.darudaserver.domain.tool.service.ToolService;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.code.SuccessCode;

import com.daruda.darudaserver.global.error.exception.InvalidValueException;
import com.daruda.darudaserver.global.handler.ValidatorUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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

    /**
     * 툴 리스트 조회
     */
    @GetMapping()
    public ResponseEntity<ApiResponse<?>> getToolList(@RequestParam(defaultValue = "인기순") String sort,
                                                      @RequestParam(defaultValue = "전체") String category,
                                                      @RequestParam(defaultValue = "1")   int page,
                                                      @RequestParam(defaultValue = "18")  int size
                                                             ){
        ValidatorUtil.validatePage(page);
        ValidatorUtil.validateSize(size, 18);

        Pageable pageable = PageRequest.of(page - 1, size);
        Category categoryEnum = Category.fromKoreanName(category);
        ToolListRes toolListRes = toolService.
                getToolList(sort , categoryEnum, pageable);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(toolListRes,SuccessCode.SUCCESS_FETCH));
    }

}
