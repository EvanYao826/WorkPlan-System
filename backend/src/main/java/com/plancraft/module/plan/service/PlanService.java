package com.plancraft.module.plan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plancraft.common.exception.BusinessException;
import com.plancraft.framework.chain.HandlerChain;
import com.plancraft.framework.statemachine.StateMachine;
import com.plancraft.module.plan.entity.Plan;
import com.plancraft.module.plan.enums.PlanEvent;
import com.plancraft.module.plan.enums.PlanStatus;
import com.plancraft.module.plan.enums.PlanType;
import com.plancraft.module.plan.mapper.PlanMapper;
import com.plancraft.module.plan.strategy.DayPlanSubmitStrategy;
import com.plancraft.module.plan.strategy.MonthPlanSubmitStrategy;
import com.plancraft.module.plan.strategy.PlanSubmitStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlanService extends ServiceImpl<PlanMapper, Plan> {

    private final StateMachine<PlanStatus, PlanEvent> planStateMachine;
    private final PlanApprovalLogService approvalLogService;

    @Lazy
    private final HandlerChain<Plan> planHandlerChain;

    // 策略路由：按 planType 获取对应策略
    private final DayPlanSubmitStrategy dayPlanSubmitStrategy;
    private final MonthPlanSubmitStrategy monthPlanSubmitStrategy;

    /**
     * 分页查询计划列表
     * @param userId 按创建人筛选（同事查自己的）
     * @param approveLeaderId 按审批领导筛选（领导查分配给自己的）
     */
    public IPage<Plan> listPlans(Long userId, Long approveLeaderId,
                                 Integer type, Integer status,
                                 LocalDate startDate, LocalDate endDate,
                                 String title, int page, int size) {
        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, Plan::getUserId, userId);
        wrapper.eq(approveLeaderId != null, Plan::getApproveLeaderId, approveLeaderId);
        wrapper.eq(type != null, Plan::getType, type);
        wrapper.eq(status != null, Plan::getStatus, status);
        wrapper.ge(startDate != null, Plan::getPlanDate, startDate);
        wrapper.le(endDate != null, Plan::getPlanDate, endDate);
        wrapper.like(StringUtils.hasText(title), Plan::getTitle, title);
        wrapper.orderByDesc(Plan::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 创建计划（保存草稿）
     */
    @Transactional
    public Plan createPlan(Plan plan, Long userId) {
        plan.setUserId(userId);
        plan.setStatus(PlanStatus.DRAFT.getCode());
        plan.setRejectCount(0);
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        plan.setVersion(1);
        save(plan);
        return plan;
    }

    /**
     * 提交审批
     * 流程：责任链校验 → 策略校验 → 状态机扭转 → 持久化 → 写日志
     */
    @Transactional
    public void submit(Long planId, Long userId, Long approveLeaderId) {
        Plan plan = getByIdOrThrow(planId);

        // 权限校验：只能提交自己的计划
        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("只能操作自己的计划");
        }

        // 必须选择审批领导
        if (approveLeaderId == null) {
            throw new BusinessException("请选择审批领导");
        }

        // 1. 责任链校验
        planHandlerChain.doChain(plan);

        // 2. 日/月计划策略校验
        getStrategy(plan.getType()).validate(plan);

        // 3. 状态机扭转
        PlanStatus fromStatus = PlanStatus.values()[plan.getStatus()];
        PlanStatus toStatus = planStateMachine.fire(fromStatus, PlanEvent.SUBMIT);

        // 4. 持久化
        plan.setStatus(toStatus.getCode());
        plan.setApproveLeaderId(approveLeaderId);
        plan.setSubmitTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        updateById(plan);

        // 5. 写审批日志
        approvalLogService.log(planId, userId, "SUBMIT",
                fromStatus.getCode(), toStatus.getCode(), null);
    }

    /**
     * 审批通过
     */
    @Transactional
    public void approve(Long planId, Long leaderId, String comment) {
        Plan plan = getByIdOrThrow(planId);

        PlanStatus fromStatus = PlanStatus.values()[plan.getStatus()];
        PlanStatus toStatus = planStateMachine.fire(fromStatus, PlanEvent.APPROVE);

        plan.setStatus(toStatus.getCode());
        plan.setApproveLeaderId(leaderId);
        plan.setApproveTime(LocalDateTime.now());
        plan.setApproveComment(comment);
        plan.setUpdateTime(LocalDateTime.now());
        updateById(plan);

        approvalLogService.log(planId, leaderId, "APPROVE",
                fromStatus.getCode(), toStatus.getCode(), comment);
    }

    /**
     * 审批驳回
     */
    @Transactional
    public void reject(Long planId, Long leaderId, String comment) {
        Plan plan = getByIdOrThrow(planId);

        PlanStatus fromStatus = PlanStatus.values()[plan.getStatus()];
        PlanStatus toStatus = planStateMachine.fire(fromStatus, PlanEvent.REJECT);

        plan.setStatus(toStatus.getCode());
        plan.setApproveLeaderId(leaderId);
        plan.setApproveTime(LocalDateTime.now());
        plan.setApproveComment(comment);
        plan.setRejectCount(plan.getRejectCount() + 1);
        plan.setUpdateTime(LocalDateTime.now());
        updateById(plan);

        approvalLogService.log(planId, leaderId, "REJECT",
                fromStatus.getCode(), toStatus.getCode(), comment);
    }

    /**
     * 撤回（待审批 → 草稿）
     */
    @Transactional
    public void withdraw(Long planId, Long userId) {
        Plan plan = getByIdOrThrow(planId);

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("只能操作自己的计划");
        }

        PlanStatus fromStatus = PlanStatus.values()[plan.getStatus()];
        PlanStatus toStatus = planStateMachine.fire(fromStatus, PlanEvent.WITHDRAW);

        plan.setStatus(toStatus.getCode());
        plan.setUpdateTime(LocalDateTime.now());
        updateById(plan);

        approvalLogService.log(planId, userId, "WITHDRAW",
                fromStatus.getCode(), toStatus.getCode(), null);
    }

    // --- 内部方法 ---

    private Plan getByIdOrThrow(Long id) {
        Plan plan = getById(id);
        if (plan == null) {
            throw new BusinessException("计划不存在");
        }
        return plan;
    }

    private PlanSubmitStrategy getStrategy(Integer type) {
        if (type == PlanType.DAY.getCode()) {
            return dayPlanSubmitStrategy;
        } else if (type == PlanType.MONTH.getCode()) {
            return monthPlanSubmitStrategy;
        }
        throw new BusinessException("未知的计划类型: " + type);
    }
}
