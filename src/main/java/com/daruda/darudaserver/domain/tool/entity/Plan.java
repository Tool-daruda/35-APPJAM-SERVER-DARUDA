package com.daruda.darudaserver.domain.tool.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.w3c.dom.Text;
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
    private int priceMonthly;

    @Column(name = "price_annual",nullable = false)
    private int priceAnnual;

    @Column(name = "description",nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tool_id",nullable = false)
    private Tool tool;

}
