package com.plancraft.module.plan.config;

import com.plancraft.framework.statemachine.StateMachine;
import com.plancraft.module.plan.enums.PlanEvent;
import com.plancraft.module.plan.enums.PlanStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 计划模块状态机配置
 * 注册所有合法的状态转换规则
 *
 * 转换规则：
 *   DRAFT   + SUBMIT   → PENDING
 *   PENDING + APPROVE  → APPROVED
 *   PENDING + REJECT   → REJECTED
 *   PENDING + WITHDRAW → DRAFT
 *   REJECTED + SUBMIT  → PENDING
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
}
