package com.plancraft.framework.strategy;

/**
 * 策略接口
 * 业务模块实现此接口定义差异化逻辑
 *
 * @param <T> 策略处理的参数类型
 * @param <R> 策略返回的结果类型
 */
public interface Strategy<T, R> {

    /**
     * 策略类型标识（用于路由选择）
     */
    String getType();

    /**
     * 执行策略
     */
    R execute(T param);
}
