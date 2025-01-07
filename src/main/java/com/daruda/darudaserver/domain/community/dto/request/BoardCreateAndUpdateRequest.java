package com.daruda.darudaserver.domain.community.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public record BoardCreateAndUpdateRequest(
        @Size(min=1, max=20) //최소 한 글자 ,띄어쓰기 포함 최대 20자 가능
        String title,
        @Size(min=1, max=10000) //최소 한 글자 ,띄어쓰기 포함 최대 10000자 가능
        @NotNull(message="내용  입력은 필수 입니다.")
        String content,
        @NotNull(message="툴 선택은 필수 입니다.")
        Long toolId,
        List<MultipartFile> images
) {
}
