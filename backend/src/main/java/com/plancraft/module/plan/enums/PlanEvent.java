package com.plancraft.module.plan.enums;

import com.plancraft.framework.statemachine.Event;
import lombok.Getter;

@Getter
public enum PlanEvent implements Event {

    SUBMIT(1, "提交"),
    APPROVE(2, "通过"),
    REJECT(3, "驳回"),
    WITHDRAW(4, "撤回");

    private final int code;
    private final String name;

    PlanEvent(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
