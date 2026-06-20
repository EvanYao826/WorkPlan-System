package com.plancraft.module.plan.chain;

import com.plancraft.framework.chain.Handler;
import com.plancraft.module.plan.entity.Plan;
import org.springframework.stereotype.Component;

/**
 * 内容校验：标题和内容不能为空
 */
@Component
public class ContentValidator implements Handler<Plan> {

    @Override
    public boolean handle(Plan plan) {
        return plan.getTitle() != null && !plan.getTitle().isBlank()
                && plan.getContent() != null && !plan.getContent().isBlank();
    }

    @Override
    public String errorMessage() {
        return "计划标题和内容不能为空";
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
