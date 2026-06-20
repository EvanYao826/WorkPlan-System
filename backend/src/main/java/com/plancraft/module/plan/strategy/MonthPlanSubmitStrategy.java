package com.plancraft.module.plan.strategy;

import com.plancraft.common.exception.BusinessException;
import com.plancraft.module.plan.entity.Plan;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 月计划提交策略
 * 校验：计划日期必须是当月第一天
 */
@Component
public class MonthPlanSubmitStrategy implements PlanSubmitStrategy {

    @Override
    public String getType() {
        return "MONTH";
    }

    @Override
    public void validate(Plan plan) {
        if (plan.getPlanDate() == null) {
            throw new BusinessException("月计划必须指定日期");
        }
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        if (!plan.getPlanDate().equals(firstDayOfMonth)) {
            throw new BusinessException("月计划的日期必须是当月第一天（" + firstDayOfMonth + "）");
        }
    }
}
