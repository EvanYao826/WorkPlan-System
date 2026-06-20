package com.plancraft.module.plan.strategy;

import com.plancraft.common.exception.BusinessException;
import com.plancraft.module.plan.entity.Plan;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 日计划提交策略
 * 校验：计划日期必须是今天
 */
@Component
public class DayPlanSubmitStrategy implements PlanSubmitStrategy {

    @Override
    public String getType() {
        return "DAY";
    }

    @Override
    public void validate(Plan plan) {
        if (plan.getPlanDate() == null) {
            throw new BusinessException("日计划必须指定日期");
        }
        if (!plan.getPlanDate().equals(LocalDate.now())) {
            throw new BusinessException("日计划的日期必须是今天");
        }
    }
}
