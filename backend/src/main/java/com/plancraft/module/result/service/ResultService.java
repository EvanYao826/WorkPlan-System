package com.plancraft.module.result.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plancraft.common.exception.BusinessException;
import com.plancraft.framework.chain.HandlerChain;
import com.plancraft.framework.statemachine.StateMachine;
import com.plancraft.module.result.entity.PlanResult;
import com.plancraft.module.result.enums.ResultEvent;
import com.plancraft.module.result.enums.ResultStatus;
import com.plancraft.module.result.mapper.ResultMapper;
import com.plancraft.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResultService extends ServiceImpl<ResultMapper, PlanResult> {

    private final StateMachine<ResultStatus, ResultEvent> resultStateMachine;
    private final ResultApprovalLogService approvalLogService;
    private final NotificationService notificationService;

    @Lazy
    private final HandlerChain<PlanResult> resultHandlerChain;

    /**
     * 分页查询成果列表
     */
    public IPage<PlanResult> listResults(Long userId, Long approveLeaderId,
                                         Integer status, int page, int size) {
        LambdaQueryWrapper<PlanResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, PlanResult::getUserId, userId);
        wrapper.eq(approveLeaderId != null, PlanResult::getApproveLeaderId, approveLeaderId);
        wrapper.eq(status != null, PlanResult::getStatus, status);
        wrapper.orderByDesc(PlanResult::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 创建成果（保存草稿或直接提交）
     */
    @Transactional
    public PlanResult createResult(PlanResult result, Long userId) {
        // 校验：同一个计划不能重复提交成果（草稿/待审批/已通过的成果存在时不允许）
        if (result.getPlanId() != null) {
            long existCount = count(new LambdaQueryWrapper<PlanResult>()
                    .eq(PlanResult::getPlanId, result.getPlanId())
                    .eq(PlanResult::getUserId, userId)
                    .in(PlanResult::getStatus,
                            ResultStatus.DRAFT.getCode(),
                            ResultStatus.PENDING.getCode(),
                            ResultStatus.APPROVED.getCode()));
            if (existCount > 0) {
                throw new BusinessException("该计划已有成果（草稿/待审批/已通过），不能重复提交");
            }
        }

        result.setUserId(userId);
        result.setStatus(ResultStatus.DRAFT.getCode());
        result.setRejectCount(0);
        result.setCreateTime(LocalDateTime.now());
        result.setUpdateTime(LocalDateTime.now());
        result.setVersion(1);
        save(result);
        return result;
    }

    /**
     * 提交审批
     */
    @Transactional
    public void submit(Long resultId, Long userId, Long approveLeaderId) {
        PlanResult result = getByIdOrThrow(resultId);

        if (!result.getUserId().equals(userId)) {
            throw new BusinessException("只能操作自己的成果");
        }
        if (approveLeaderId == null) {
            throw new BusinessException("请选择审批领导");
        }

        // 责任链校验
        resultHandlerChain.doChain(result);

        // 状态机扭转
        ResultStatus fromStatus = ResultStatus.values()[result.getStatus()];
        ResultStatus toStatus = resultStateMachine.fire(fromStatus, ResultEvent.SUBMIT);

        result.setStatus(toStatus.getCode());
        result.setApproveLeaderId(approveLeaderId);
        result.setSubmitTime(LocalDateTime.now());
        result.setUpdateTime(LocalDateTime.now());
        updateById(result);

        approvalLogService.log(resultId, userId, "SUBMIT",
                fromStatus.getCode(), toStatus.getCode(), null);

        // 通知领导：有新成果待审批
        notificationService.sendNotification(approveLeaderId,
                "新成果待审批", "员工提交了成果《" + result.getTitle() + "》，等待您的审批",
                "RESULT_SUBMITTED", resultId, "RESULT");
    }

    /**
     * 审批通过
     */
    @Transactional
    public void approve(Long resultId, Long leaderId, String comment) {
        PlanResult result = getByIdOrThrow(resultId);

        ResultStatus fromStatus = ResultStatus.values()[result.getStatus()];
        ResultStatus toStatus = resultStateMachine.fire(fromStatus, ResultEvent.APPROVE);

        result.setStatus(toStatus.getCode());
        result.setApproveLeaderId(leaderId);
        result.setApproveTime(LocalDateTime.now());
        result.setApproveComment(comment);
        result.setUpdateTime(LocalDateTime.now());
        updateById(result);

        approvalLogService.log(resultId, leaderId, "APPROVE",
                fromStatus.getCode(), toStatus.getCode(), comment);

        // 通知员工：成果已通过
        notificationService.sendNotification(result.getUserId(),
                "成果审批通过", "您的成果《" + result.getTitle() + "》已通过审批",
                "RESULT_APPROVED", resultId, "RESULT");
    }

    /**
     * 审批驳回
     */
    @Transactional
    public void reject(Long resultId, Long leaderId, String comment) {
        PlanResult result = getByIdOrThrow(resultId);

        ResultStatus fromStatus = ResultStatus.values()[result.getStatus()];
        ResultStatus toStatus = resultStateMachine.fire(fromStatus, ResultEvent.REJECT);

        result.setStatus(toStatus.getCode());
        result.setApproveLeaderId(leaderId);
        result.setApproveTime(LocalDateTime.now());
        result.setApproveComment(comment);
        result.setRejectCount(result.getRejectCount() + 1);
        result.setUpdateTime(LocalDateTime.now());
        updateById(result);

        approvalLogService.log(resultId, leaderId, "REJECT",
                fromStatus.getCode(), toStatus.getCode(), comment);

        // 通知员工：成果已驳回
        String rejectContent = "您的成果《" + result.getTitle() + "》已被驳回";
        if (comment != null && !comment.isBlank()) {
            rejectContent += "，原因：" + comment;
        }
        notificationService.sendNotification(result.getUserId(),
                "成果审批驳回", rejectContent,
                "RESULT_REJECTED", resultId, "RESULT");
    }

    /**
     * 撤回
     */
    @Transactional
    public void withdraw(Long resultId, Long userId) {
        PlanResult result = getByIdOrThrow(resultId);

        if (!result.getUserId().equals(userId)) {
            throw new BusinessException("只能操作自己的成果");
        }

        ResultStatus fromStatus = ResultStatus.values()[result.getStatus()];
        ResultStatus toStatus = resultStateMachine.fire(fromStatus, ResultEvent.WITHDRAW);

        result.setStatus(toStatus.getCode());
        result.setUpdateTime(LocalDateTime.now());
        updateById(result);

        approvalLogService.log(resultId, userId, "WITHDRAW",
                fromStatus.getCode(), toStatus.getCode(), null);
    }

    private PlanResult getByIdOrThrow(Long id) {
        PlanResult result = getById(id);
        if (result == null) {
            throw new BusinessException("成果不存在");
        }
        return result;
    }
}
