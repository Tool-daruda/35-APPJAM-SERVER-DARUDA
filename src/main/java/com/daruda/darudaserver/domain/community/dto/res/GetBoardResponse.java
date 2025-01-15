package com.daruda.darudaserver.domain.community.dto.res;

import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.global.common.response.ScrollPaginationCollection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
public class GetBoardResponse {
    private static final long LAST_CURSOR = -1L;
    private List<BoardRes> contents;
    private long totalElements;
    private long nextCursor;

    public GetBoardResponse(List<BoardRes> contents, long totalElements, long nextCursor) {
        this.contents = contents;
        this.totalElements = totalElements;
        this.nextCursor = nextCursor;
    }

    public static GetBoardResponse of (ScrollPaginationCollection<Board> pagination, String toolName, String toolLogo, int commentCount, List<String> boardImages){
        List<BoardRes> boardResList = pagination.getCurrentScrollItems().stream().map(board -> BoardRes.of(
                        board,
                        toolName,
                        toolLogo,
                        commentCount,
                        boardImages
                ))
                .collect(Collectors.toList());

        long nextCursor = pagination.isLastScroll() ? LAST_CURSOR : pagination.getNextCursor().getBoardId();

        return new GetBoardResponse(boardResList, pagination.getTotalElements(), nextCursor);
    }
}

