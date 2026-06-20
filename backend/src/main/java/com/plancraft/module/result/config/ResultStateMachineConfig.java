package com.plancraft.module.result.config;

import com.plancraft.framework.chain.Handler;
import com.plancraft.framework.chain.HandlerChain;
import com.plancraft.framework.statemachine.StateMachine;
import com.plancraft.module.result.entity.PlanResult;
import com.plancraft.module.result.enums.ResultEvent;
import com.plancraft.module.result.enums.ResultStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 成果模块配置：状态机 + 责任链
 */
@Configuration
public class ResultStateMachineConfig {

    @Bean
    public StateMachine<ResultStatus, ResultEvent> resultStateMachine() {
        StateMachine<ResultStatus, ResultEvent> sm = new StateMachine<>();
        sm.addTransition(ResultStatus.DRAFT, ResultEvent.SUBMIT, ResultStatus.PENDING)
          .addTransition(ResultStatus.PENDING, ResultEvent.APPROVE, ResultStatus.APPROVED)
          .addTransition(ResultStatus.PENDING, ResultEvent.REJECT, ResultStatus.REJECTED)
          .addTransition(ResultStatus.PENDING, ResultEvent.WITHDRAW, ResultStatus.DRAFT)
          .addTransition(ResultStatus.REJECTED, ResultEvent.SUBMIT, ResultStatus.PENDING);
        return sm;
    }

    @Bean
    public HandlerChain<PlanResult> resultHandlerChain(List<Handler<PlanResult>> handlers) {
        return new HandlerChain<>(handlers);
    }
}
