package com.plancraft.module.result.config;

import com.plancraft.framework.statemachine.StateMachine;
import com.plancraft.module.result.enums.ResultEvent;
import com.plancraft.module.result.enums.ResultStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 成果模块状态机配置
 * 转换规则与计划一致
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
}
