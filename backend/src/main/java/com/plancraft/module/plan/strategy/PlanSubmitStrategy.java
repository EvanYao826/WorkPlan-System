package com.plancraft.module.plan.strategy;

import com.plancraft.module.plan.entity.Plan;

/**
 * 计划提交策略接口
 * 日/月计划的差异化校验逻辑
 */
public interface PlanSubmitStrategy {

    /**
     * 策略类型标识：DAY / MONTH
     */
    String getType();

    /**
     * 提交前的差异化校验
     * @param plan 计划
     * @throws com.plancraft.common.exception.BusinessException 校验不通过时抛异常
     */
    void validate(Plan plan);
}
