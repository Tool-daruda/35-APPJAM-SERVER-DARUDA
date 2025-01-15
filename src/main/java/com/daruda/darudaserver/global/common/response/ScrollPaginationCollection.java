package com.daruda.darudaserver.global.common.response;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ScrollPaginationCollection<T> {

    public final List <T> itemsWithNextCursor; // 현재 스크롤의 데이터 + 다음 스크롤의 데이터 1개(다음 데이터 확인 위함)
    public final int countPerScroll; // 스크롤 1회에 조회할 데이터의 개수

    public static <T> ScrollPaginationCollection <T> of (List<T> itemsWithNextCursor,
                                                        int size){
        return new ScrollPaginationCollection<>(itemsWithNextCursor,size);
    }

    // 마지막 스크롤린지 확인하기 위한 메서드, 조회한 결과(countPerScroll) 이하로 조회시 마지막 스크롤 이라고 판단
    public boolean isLastScroll(){
        return this.itemsWithNextCursor.size() <= countPerScroll;
    }

    // 마지막 스크롤 일 경우 itemsWithNextCursor를 Return
    public List<T> getCurrentScrollItems(){
        // 마지막 스크롤일 경우
        if(isLastScroll()){
            return this.itemsWithNextCursor;
        }
        // 마지막 스크롤이 아닐 경우 데이터 1개를 제외하고 Return
        return this.itemsWithNextCursor.subList(0, countPerScroll);
    }

    // 마지막 데이터를 cursor 로 사용하고 Return
    public T getNextCursor(){
        return itemsWithNextCursor.get(countPerScroll - 1);
    }

    public long getTotalElements(){
        return this.itemsWithNextCursor.size();
    }
}
