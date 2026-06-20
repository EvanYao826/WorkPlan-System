package com.plancraft.module.result.chain;

import com.plancraft.framework.chain.Handler;
import com.plancraft.module.plan.entity.Plan;
import com.plancraft.module.plan.enums.PlanStatus;
import com.plancraft.module.plan.service.PlanService;
import com.plancraft.module.result.entity.PlanResult;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 校验关联的计划状态必须是已通过
 */
@Component
public class PlanStatusValidator implements Handler<PlanResult> {

    @Lazy
    private final PlanService planService;

    public PlanStatusValidator(@Lazy PlanService planService) {
        this.planService = planService;
    }

    @Override
    public boolean handle(PlanResult result) {
        if (result.getPlanId() == null) {
            return false;
        }
        Plan plan = planService.getById(result.getPlanId());
        return plan != null && plan.getStatus() == PlanStatus.APPROVED.getCode();
    }

    @Override
    public String errorMessage() {
        return "关联的计划不存在或尚未通过审批，只能对已通过的计划提交成果";
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
