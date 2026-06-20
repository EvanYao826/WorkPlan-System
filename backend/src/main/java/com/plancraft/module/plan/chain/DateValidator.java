package com.plancraft.module.plan.chain;

import com.plancraft.framework.chain.Handler;
import com.plancraft.module.plan.entity.Plan;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 日期校验：计划日期不能为空，不能是过去日期
 */
@Component
public class DateValidator implements Handler<Plan> {

    @Override
    public boolean handle(Plan plan) {
        if (plan.getPlanDate() == null) {
            return false;
        }
        // 允许今天及以后的日期
        return !plan.getPlanDate().isBefore(LocalDate.now());
    }

    @Override
    public String errorMessage() {
        return "计划日期不能为空且不能早于今天";
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
