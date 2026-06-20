package com.plancraft.framework.statemachine;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 状态转换规则：源状态 + 事件 -> 目标状态
 */
@Data
@AllArgsConstructor
public class Transition<S extends State, E extends Event> {

    private S sourceState;
    private E event;
    private S targetState;
}
