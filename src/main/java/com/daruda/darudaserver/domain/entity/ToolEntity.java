package com.daruda.darudaserver.domain.entity;

import com.daruda.darudaserver.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.w3c.dom.Text;

import java.time.LocalDateTime;

@Entity
@Table(name="tool")
@RequiredArgsConstructor
public class ToolEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long toolId;

    @NotNull
    private final String toolLogo;

    @NotNull
    private final String toolMainName;

    @NotNull
    private final String toolNameSub;

    @NotNull
    private final String toolLink;

    @NotNull
    private final String description;

    @NotNull
    private final String license;

    @NotNull
    private final boolean supportKor;

    @NotNull
    private final Text detailDescription;

    @NotNull
    private final LocalDateTime updatedAt = LocalDateTime.now();

    @NotNull
    private final String color;

    private final String planLink;
}
