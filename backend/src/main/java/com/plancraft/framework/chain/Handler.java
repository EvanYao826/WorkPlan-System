package com.plancraft.framework.chain;

/**
 * 责任链处理器接口
 *
 * @param <T> 处理的参数类型
 */
public interface Handler<T> {

    /**
     * 执行校验/处理
     * @param param 参数
     * @return true 继续下一个处理器，false 中断链
     */
    boolean handle(T param);

    /**
     * 失败时的错误消息
     */
    default String errorMessage() {
        return "校验失败";
    }

    /**
     * 处理器顺序（越小越先执行）
     */
    default int getOrder() {
        return 0;
    }
}
