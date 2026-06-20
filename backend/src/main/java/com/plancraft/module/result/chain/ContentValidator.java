package com.plancraft.module.result.chain;

import com.plancraft.framework.chain.Handler;
import com.plancraft.module.result.entity.PlanResult;
import org.springframework.stereotype.Component;

/**
 * 成果内容校验：标题和内容不能为空
 */
@Component("resultContentValidator")
public class ContentValidator implements Handler<PlanResult> {

    @Override
    public boolean handle(PlanResult result) {
        return result.getTitle() != null && !result.getTitle().isBlank()
                && result.getContent() != null && !result.getContent().isBlank();
    }

    @Override
    public String errorMessage() {
        return "成果标题和内容不能为空";
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
