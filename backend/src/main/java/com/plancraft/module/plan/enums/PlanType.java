package com.plancraft.module.plan.enums;

import lombok.Getter;

@Getter
public enum PlanType {

    DAY(1, "日计划"),
    MONTH(2, "月计划");

    private final int code;
    private final String name;

    PlanType(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
