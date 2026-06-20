package com.plancraft.module.plan.config;

import com.plancraft.framework.chain.Handler;
import com.plancraft.framework.chain.HandlerChain;
import com.plancraft.framework.statemachine.StateMachine;
import com.plancraft.module.plan.entity.Plan;
import com.plancraft.module.plan.enums.PlanEvent;
import com.plancraft.module.plan.enums.PlanStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 计划模块配置：状态机 + 责任链
 */
@Configuration
public class PlanStateMachineConfig {

    @Bean
    public StateMachine<PlanStatus, PlanEvent> planStateMachine() {
        StateMachine<PlanStatus, PlanEvent> sm = new StateMachine<>();
        sm.addTransition(PlanStatus.DRAFT, PlanEvent.SUBMIT, PlanStatus.PENDING)
          .addTransition(PlanStatus.PENDING, PlanEvent.APPROVE, PlanStatus.APPROVED)
          .addTransition(PlanStatus.PENDING, PlanEvent.REJECT, PlanStatus.REJECTED)
          .addTransition(PlanStatus.PENDING, PlanEvent.WITHDRAW, PlanStatus.DRAFT)
          .addTransition(PlanStatus.REJECTED, PlanEvent.SUBMIT, PlanStatus.PENDING);
        return sm;
    }

    @Bean
    public HandlerChain<Plan> planHandlerChain(List<Handler<Plan>> handlers) {
        return new HandlerChain<>(handlers);
    }
}
