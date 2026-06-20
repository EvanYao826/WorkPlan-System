package com.plancraft.framework.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 策略路由器
 * 自动收集 Spring 容器中所有 Strategy 实现，按 type 路由
 *
 * 使用方式：
 *   @Autowired
 *   private StrategyRouter router;
 *
 *   Strategy<PlanSubmitParam, Void> strategy = router.getStrategy("DAY", PlanSubmitStrategy.class);
 *   strategy.execute(param);
 */
@Component
public class StrategyRouter {

    private final Map<String, Map<String, Strategy<?, ?>>> strategyMap = new ConcurrentHashMap<>();

    /**
     * 自动注入所有 Strategy 实现
     */
    @Autowired(required = false)
    @SuppressWarnings("unchecked")
    public void setStrategies(List<Strategy<?, ?>> strategies) {
        if (strategies == null) return;
        for (Strategy<?, ?> strategy : strategies) {
            String className = strategy.getClass().getSimpleName();
            strategyMap.computeIfAbsent(className, k -> new ConcurrentHashMap<>())
                    .put(strategy.getType(), strategy);
        }
    }

    /**
     * 按策略实现类名 + type 获取具体策略
     *
     * @param type 策略类型标识
     * @param clazz 策略接口的 Class（用于定位哪一组策略）
     */
    @SuppressWarnings("unchecked")
    public <T, R> Strategy<T, R> getStrategy(String type, Class<? extends Strategy<T, R>> clazz) {
        Map<String, Strategy<?, ?>> map = strategyMap.get(clazz.getSimpleName());
        if (map == null) {
            throw new IllegalStateException("未找到策略组: " + clazz.getSimpleName());
        }
        Strategy<?, ?> strategy = map.get(type);
        if (strategy == null) {
            throw new IllegalStateException("未找到策略类型: " + type + " in " + clazz.getSimpleName());
        }
        return (Strategy<T, R>) strategy;
    }
}
