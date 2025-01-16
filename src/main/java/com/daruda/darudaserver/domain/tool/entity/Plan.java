package com.daruda.darudaserver.domain.tool.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Getter
@Table(name="tool_plan")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    @Column(name = "plan_name",nullable = false)
    private String planName;

    @Column(name = "price_monthly",nullable = false)
    private Long priceMonthly;

    @Column(name = "price_annual")
    private Long priceAnnual;

    @Column(name = "description",nullable = false,length = 50000)
    private String description;

    @Column(name = "is_dollar",nullable = false)
    private Boolean isDollar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tool_id",nullable = false)
    private Tool tool;
}
