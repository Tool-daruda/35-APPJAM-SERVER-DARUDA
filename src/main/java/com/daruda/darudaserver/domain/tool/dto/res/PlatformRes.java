package com.daruda.darudaserver.domain.tool.dto.res;

import com.daruda.darudaserver.domain.tool.entity.ToolPlatForm;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record PlatformRes (
        Boolean web,
        Boolean windows,
        Boolean mac
) {
    public static PlatformRes of(ToolPlatForm platForm){
        return PlatformRes.builder()
                .web(platForm.getWeb())
                .windows(platForm.getWindows())
                .mac(platForm.getMac())
                .build();
    }
}
