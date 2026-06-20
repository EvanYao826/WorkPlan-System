package com.plancraft.framework.statemachine;

/**
 * 事件接口
 * 业务模块实现此接口定义具体事件枚举
 */
public interface Event {

    /**
     * 事件编码
     */
    int getCode();

    /**
     * 事件名称
     */
    String getName();
}
