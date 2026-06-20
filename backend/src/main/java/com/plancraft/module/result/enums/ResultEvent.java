package com.plancraft.module.result.enums;

import com.plancraft.framework.statemachine.Event;
import lombok.Getter;

@Getter
public enum ResultEvent implements Event {

    SUBMIT(1, "提交"),
    APPROVE(2, "通过"),
    REJECT(3, "驳回"),
    WITHDRAW(4, "撤回");

    private final int code;
    private final String name;

    ResultEvent(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
