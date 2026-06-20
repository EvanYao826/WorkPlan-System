package com.plancraft.module.plan.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plancraft.module.plan.entity.PlanApprovalLog;
import com.plancraft.module.plan.mapper.PlanApprovalLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PlanApprovalLogService extends ServiceImpl<PlanApprovalLogMapper, PlanApprovalLog> {

    /**
     * 记录审批日志
     */
    public void log(Long planId, Long operatorId, String action,
                    int fromStatus, int toStatus, String comment) {
        PlanApprovalLog log = new PlanApprovalLog();
        log.setPlanId(planId);
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setComment(comment);
        log.setCreateTime(LocalDateTime.now());
        save(log);
    }
}
