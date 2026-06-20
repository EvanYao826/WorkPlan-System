package com.plancraft.module.result.enums;

import com.plancraft.framework.statemachine.State;
import lombok.Getter;

@Getter
public enum ResultStatus implements State {

    DRAFT(0, "草稿"),
    PENDING(1, "待审批"),
    APPROVED(2, "已通过"),
    REJECTED(3, "已驳回"),
    WITHDRAWN(4, "已撤回");

    private final int code;
    private final String name;

    ResultStatus(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
