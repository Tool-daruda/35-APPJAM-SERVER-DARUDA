package com.daruda.darudaserver.domain.tool.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.w3c.dom.Text;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Getter
@Table(name="plan")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    @Column(name = "plan_name",nullable = false)
    private String planName;

    @Column(name = "plan_monthly",nullable = false)
    private int priceMonthly;

    @Column(name = "plan_annual",nullable = false)
    private int priceAnnual;

    @Column(name = "feature",nullable = false)
    private String feature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tool_id",nullable = false)
    private Tool tool;

}
