package com.plancraft.framework.chain;

import com.plancraft.common.exception.BusinessException;

import java.util.List;

/**
 * 责任链编排器
 * 按 order 排序后依次执行，任一失败则中断
 *
 * 不再作为通用 @Component，由各模块在自己的 Config 中创建 Bean
 */
public class HandlerChain<T> {

    private final List<Handler<T>> handlers;

    public HandlerChain(List<Handler<T>> handlers) {
        this.handlers = handlers.stream()
                .sorted(java.util.Comparator.comparingInt(Handler::getOrder))
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
}
