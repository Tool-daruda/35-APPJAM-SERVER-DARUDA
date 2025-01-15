package com.daruda.darudaserver.domain.community.entity;

import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.global.common.entity.BaseTimeEntity;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.lang.Nullable;;

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

    @Column(name="tool_id",nullable = true)
    private Long toolId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private UserEntity user;

    @Builder.Default
    private boolean isFree=true;

    @Builder
    public Board(final String title,final String content, final Long toolId, final UserEntity user, final boolean delYn,final boolean isFree) {
        this.title = title;
        this.content = content;
        this.toolId = toolId;
        this.user = user;
        this.delYn = delYn;
        this.isFree = isFree;
    }

    public static Board create(final Long toolId, final UserEntity user, final String title, final String content){
        if (toolId == null) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST_DATA);

        }
        return Board.builder()
                .toolId(toolId)
                .user(user)
                .title(title)
                .content(content)
                .isFree(false)
                .build();
    }

    public static Board createFree( final UserEntity user, final String title, final String content){
        return Board.builder()
                .toolId(null)
                .user(user)
                .title(title)
                .content(content)
                .isFree(true)
                .build();
    }

    public void update(final Long toolId, final UserEntity user, final String title, final String content, final boolean isFree) {
        this.toolId = toolId;
        this.user = user;
        this.title = title;
        this.content = content;
        this.isFree = isFree;
    }

    public void delete(){
        this.delYn=true;
    }
}
