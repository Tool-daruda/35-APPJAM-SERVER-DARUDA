package com.daruda.darudaserver.domain.user.controller;

import com.daruda.darudaserver.domain.user.dto.request.UpdateMyRequest;
import com.daruda.darudaserver.domain.user.dto.response.UpdateMyResponse;
import com.daruda.darudaserver.domain.user.service.UserService;
import com.daruda.darudaserver.global.auth.UserId;
import com.daruda.darudaserver.global.common.response.ApiResponse;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.code.SuccessCode;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MyPageController {
    private final UserService userService;

    @PatchMapping
    public ResponseEntity<?> patchMy(@UserId Long userId,
                                     @Valid @RequestBody UpdateMyRequest updateMyRequest){
        if(updateMyRequest.positions() ==null && updateMyRequest.nickname() ==null){
            throw new BusinessException(ErrorCode.MISSING_PARAMETER);
        }
        UpdateMyResponse updateMyResponse = userService.updateMy(userId,updateMyRequest.nickname(),updateMyRequest.positions());
        return ResponseEntity.ok(ApiResponse.ofSuccessWithData(updateMyResponse, SuccessCode.SUCCESS_UPDATE));
    }
/*
    @GetMapping("/tools")
    public ResponseEntity<?> getFavoriteTools(@UserId Long userId,
                                              @RequestParam(defaultValue = "1", value = "page")int pageNo,
                                              @RequestParam(defaultValue = "createdAt", value = "criteria")String criteria){
        userService.getFavoriteTools(userId, pageNo, criteria);


    }
 */
}
