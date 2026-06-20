package com.plancraft.module.result.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plancraft.module.result.entity.ResultApprovalLog;
import com.plancraft.module.result.mapper.ResultApprovalLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ResultApprovalLogService extends ServiceImpl<ResultApprovalLogMapper, ResultApprovalLog> {

    public void log(Long resultId, Long operatorId, String action,
                    int fromStatus, int toStatus, String comment) {
        ResultApprovalLog log = new ResultApprovalLog();
        log.setResultId(resultId);
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setComment(comment);
        log.setCreateTime(LocalDateTime.now());
        save(log);
    }
}
