package com.plancraft.module.plan.strategy;

import com.plancraft.module.plan.entity.Plan;
import org.springframework.stereotype.Component;

/**
 * 月计划提交策略
 * 月计划无额外限制，通用校验已由 DateValidator 处理
 */
@Component
public class MonthPlanSubmitStrategy implements PlanSubmitStrategy {

    @Override
    public String getType() {
        return "MONTH";
    }

    @Override
    public void validate(Plan plan) {
        // 月计划无特殊限制，日期合法性由 DateValidator 统一校验
    }
}
