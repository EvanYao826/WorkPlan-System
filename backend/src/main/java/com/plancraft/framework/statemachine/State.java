package com.plancraft.framework.statemachine;

/**
 * 状态接口
 * 业务模块实现此接口定义具体状态枚举
 */
public interface State {

    /**
     * 状态编码（用于持久化）
     */
    int getCode();

    /**
     * 状态名称
     */
    String getName();
}
