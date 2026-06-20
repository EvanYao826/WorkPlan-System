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
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResultService extends ServiceImpl<ResultMapper, PlanResult> {

    private final StateMachine<ResultStatus, ResultEvent> resultStateMachine;
    private final ResultApprovalLogService approvalLogService;

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
