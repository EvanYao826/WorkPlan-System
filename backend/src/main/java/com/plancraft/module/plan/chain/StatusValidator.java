package com.plancraft.module.plan.chain;

import com.plancraft.framework.chain.Handler;
import com.plancraft.module.plan.entity.Plan;
import com.plancraft.module.plan.enums.PlanStatus;
import org.springframework.stereotype.Component;

/**
 * 状态校验：只有草稿或驳回状态的计划才能提交
 */
@Component
public class StatusValidator implements Handler<Plan> {

    @Override
    public boolean handle(Plan plan) {
        if (plan.getStatus() == null) {
            return false;
        }
        return plan.getStatus() == PlanStatus.DRAFT.getCode()
                || plan.getStatus() == PlanStatus.REJECTED.getCode();
    }

    @Override
    public String errorMessage() {
        return "当前状态不允许提交，只有草稿或已驳回的计划才能提交";
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
