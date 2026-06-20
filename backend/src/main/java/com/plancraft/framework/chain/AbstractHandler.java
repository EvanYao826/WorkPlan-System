package com.plancraft.framework.chain;

import lombok.Setter;

/**
 * 抽象处理器 — 提供链式调用能力
 */
public abstract class AbstractHandler<T> implements Handler<T> {

    @Setter
    private Handler<T> next;

    @Override
    public boolean handle(T param) {
        if (!doHandle(param)) {
            return false;
        }
        return next == null || next.handle(param);
    }

    /**
     * 子类实现具体的校验/处理逻辑
     */
    protected abstract boolean doHandle(T param);
}
