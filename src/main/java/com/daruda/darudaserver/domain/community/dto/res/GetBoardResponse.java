package com.daruda.darudaserver.domain.community.dto.res;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.global.common.response.ScrollPaginationCollection;

import java.util.List;
import java.util.stream.Collectors;

public record GetBoardResponse(
        List<BoardRes> contents,
        long totalElements,
        long nextCursor
) {
    private static final long LAST_CURSOR = -1L;

    public static GetBoardResponse of(
            ScrollPaginationCollection<Board> pagination,
            String toolName,
            String toolLogo,
            int commentCount,
            List<String> boardImages
    ) {
        List<BoardRes> boardResList = pagination.getCurrentScrollItems().stream()
                .map(board -> BoardRes.of(board, toolName, toolLogo, commentCount, boardImages))
                .collect(Collectors.toList());

        long nextCursor = pagination.isLastScroll() ? LAST_CURSOR : pagination.getNextCursor().getId();

        return new GetBoardResponse(boardResList, pagination.getTotalElements(), nextCursor);
    }
}
