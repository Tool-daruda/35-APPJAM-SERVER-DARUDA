package com.daruda.darudaserver.domain.community.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Builder(access = AccessLevel.PROTECTED)
public record BoardCreateAndUpdateReq(
        @NotNull(message="제목 입력 필수 입니다.")
        @Size(min=1, max=20, message = "제목은 최소 한 글자, 최대 20글자이어야 합니다")//최소 한 글자 ,띄어쓰기 포함 최대 20자 가능
        String title,
        @Size(min=1, max=10000) //최소 한 글자 ,띄어쓰기 포함 최대 10000자 가능
        @NotNull(message="내용  입력은 필수 입니다.")
        String content,
        @NotNull(message="툴 선택은 필수 입니다.")
        Long toolId
) {
        public static BoardCreateAndUpdateReq of(String title, String content, Long toolId) {
                return BoardCreateAndUpdateReq.builder()
                        .title(title)
                        .content(content)
                        .toolId(toolId)
                        .build();
        }
}
