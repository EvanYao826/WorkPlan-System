package com.plancraft.module.user.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum Role {

    EMPLOYEE("EMPLOYEE", "员工"),
    LEADER("LEADER", "领导");

    private final String code;
    private final String name;

    Role(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
