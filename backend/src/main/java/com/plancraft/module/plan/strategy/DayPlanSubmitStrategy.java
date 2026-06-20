package com.plancraft.module.plan.strategy;

import com.plancraft.module.plan.entity.Plan;
import org.springframework.stereotype.Component;

/**
 * 日计划提交策略
 * 日计划无额外限制，通用校验已由 DateValidator 处理
 */
@Component
public class DayPlanSubmitStrategy implements PlanSubmitStrategy {

    @Override
    public String getType() {
        return "DAY";
    }

    @Override
    public void validate(Plan plan) {
        // 日计划无特殊限制，日期合法性由 DateValidator 统一校验
    }
}
