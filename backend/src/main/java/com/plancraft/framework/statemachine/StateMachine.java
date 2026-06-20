package com.plancraft.framework.statemachine;

import com.plancraft.common.exception.BusinessException;
import com.plancraft.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 有限状态机引擎
 * 通过注册转换规则，校验状态流转的合法性
 *
 * 使用方式：
 *   StateMachine<PlanState, PlanEvent> sm = new StateMachine<>();
 *   sm.addTransition(PlanState.DRAFT, PlanEvent.SUBMIT, PlanState.PENDING);
 *   PlanState next = sm.fire(PlanState.DRAFT, PlanEvent.SUBMIT);
 */
@Slf4j
public class StateMachine<S extends State, E extends Event> {

    /**
     * 转换规则表：key = "sourceStateCode:eventCode", value = targetState
     */
    private final Map<String, S> transitionTable = new HashMap<>();

    /**
     * 注册一条转换规则
     */
    public StateMachine<S, E> addTransition(S source, E event, S target) {
        String key = buildKey(source.getCode(), event.getCode());
        transitionTable.put(key, target);
        return this;
    }

    /**
     * 触发事件，返回目标状态
     * 如果当前状态不允许该事件，抛出 BusinessException
     */
    public S fire(S currentState, E event) {
        String key = buildKey(currentState.getCode(), event.getCode());
        S target = transitionTable.get(key);

        if (target == null) {
            log.warn("非法状态流转: state={}, event={}", currentState.getName(), event.getName());
            throw new BusinessException(
                    ResultCode.STATE_TRANSITION_ERROR.getCode(),
                    String.format("当前状态[%s]不允许执行[%s]操作", currentState.getName(), event.getName())
            );
        }

        log.debug("状态流转: {} + {} -> {}", currentState.getName(), event.getName(), target.getName());
        return target;
    }

    /**
     * 判断当前状态是否允许执行该事件
     */
    public boolean canFire(S currentState, E event) {
        return transitionTable.containsKey(buildKey(currentState.getCode(), event.getCode()));
    }

    private String buildKey(int stateCode, int eventCode) {
        return stateCode + ":" + eventCode;
    }
}
