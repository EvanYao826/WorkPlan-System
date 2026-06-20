package com.plancraft.framework.chain;

import com.plancraft.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 责任链编排器
 * 自动收集 Spring 容器中所有 Handler 实现，按 order 排序后依次执行
 *
 * 使用方式：
 *   @Autowired
 *   private HandlerChain<PlanSubmitParam> planSubmitChain;
 *
 *   planSubmitChain.doChain(param); // 全部通过才继续，否则抛 BusinessException
 */
@Component
public class HandlerChain<T> {

    private List<Handler<T>> handlers;

    @Autowired
    @SuppressWarnings("unchecked")
    public void setHandlers(List<Handler<?>> allHandlers) {
        this.handlers = allHandlers.stream()
                .filter(h -> matchesType(h))
                .map(h -> (Handler<T>) h)
                .sorted(Comparator.comparingInt(Handler::getOrder))
                .toList();
    }

    /**
     * 执行责任链，任一处理器失败则抛出 BusinessException
     */
    public void doChain(T param) {
        for (Handler<T> handler : handlers) {
            if (!handler.handle(param)) {
                throw new BusinessException(handler.errorMessage());
            }
        }
    }

    /**
     * 判断 handler 是否匹配泛型类型 T
     * 子类可通过覆写此方法自定义过滤逻辑
     */
    @SuppressWarnings("unchecked")
    protected boolean matchesType(Handler<?> handler) {
        // 默认收集所有 handler，实际使用时建议按模块分组
        return true;
    }
}
