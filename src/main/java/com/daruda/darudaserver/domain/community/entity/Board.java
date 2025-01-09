package com.daruda.darudaserver.domain.community.entity;

import com.daruda.darudaserver.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access=AccessLevel.PROTECTED)
public class Board extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardId;

    @NotNull
    private String title;

    @NotNull
    private String content;

    @NotNull
    @Builder.Default
    private boolean delYn = false;

    @NotNull
    private Long toolId;

    @NotNull
    private Long userId;

    @Builder
    public Board(final String title,final String content, final Long toolId, final Long userId, final boolean delYn) {
        this.title = title;
        this.content = content;
        this.toolId = toolId;
        this.userId = userId;
        this.delYn = delYn;
    }

    public static Board create(final Long toolId, final Long userId, final String title, final String content){
        return Board.builder()
                .toolId(toolId)
                .userId(userId)
                .title(title)
                .content(content)
                .build();
    }

    public static Board update(final Long boardId , final Long toolId, final Long userId, final String title, final String content){
        return Board.builder()
                .boardId(boardId)
                .toolId(toolId)
                .userId(userId)
                .title(title)
                .content(content)
                .build();
    }
    public void delete(){
        this.delYn=true;
    }
}
