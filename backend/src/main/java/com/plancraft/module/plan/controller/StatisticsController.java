package com.plancraft.module.plan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plancraft.common.result.R;
import com.plancraft.module.plan.entity.Plan;
import com.plancraft.module.plan.enums.PlanStatus;
import com.plancraft.module.plan.service.PlanService;
import com.plancraft.module.result.entity.PlanResult;
import com.plancraft.module.result.service.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "统计分析", description = "Dashboard 统计数据")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final PlanService planService;
    private final ResultService resultService;

    /**
     * 获取当前用户的统计数据
     */
    @Operation(summary = "获取统计数据", description = "按角色返回计划/成果各状态数量")
    @GetMapping
    public R<Map<String, Object>> statistics(Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        Map<String, Object> stats = new LinkedHashMap<>();

        if ("ROLE_LEADER".equals(role)) {
            // 领导：按审批领导ID筛选
            long pendingPlans = countByApproverAndStatus(userId, PlanStatus.PENDING);
            long approvedPlans = countByApproverAndStatus(userId, PlanStatus.APPROVED);
            long rejectedPlans = countByApproverAndStatus(userId, PlanStatus.REJECTED);
            long pendingResults = countResultByApproverAndStatus(userId, PlanStatus.PENDING);
            long approvedResults = countResultByApproverAndStatus(userId, PlanStatus.APPROVED);
            long rejectedResults = countResultByApproverAndStatus(userId, PlanStatus.REJECTED);

            stats.put("pendingPlans", pendingPlans);
            stats.put("approvedPlans", approvedPlans);
            stats.put("rejectedPlans", rejectedPlans);
            stats.put("pendingResults", pendingResults);
            stats.put("approvedResults", approvedResults);
            stats.put("rejectedResults", rejectedResults);
            stats.put("pendingTotal", pendingPlans + pendingResults);
        } else {
            // 同事：按创建人ID筛选
            long draftPlans = countByUserAndStatus(userId, PlanStatus.DRAFT);
            long pendingPlans = countByUserAndStatus(userId, PlanStatus.PENDING);
            long approvedPlans = countByUserAndStatus(userId, PlanStatus.APPROVED);
            long rejectedPlans = countByUserAndStatus(userId, PlanStatus.REJECTED);
            long withdrawnPlans = countByUserAndStatus(userId, PlanStatus.WITHDRAWN);

            long draftResults = countResultByUserAndStatus(userId, PlanStatus.DRAFT);
            long pendingResults = countResultByUserAndStatus(userId, PlanStatus.PENDING);
            long approvedResults = countResultByUserAndStatus(userId, PlanStatus.APPROVED);
            long rejectedResults = countResultByUserAndStatus(userId, PlanStatus.REJECTED);
            long withdrawnResults = countResultByUserAndStatus(userId, PlanStatus.WITHDRAWN);

            stats.put("myPlans", draftPlans + pendingPlans + approvedPlans + rejectedPlans + withdrawnPlans);
            stats.put("myResults", draftResults + pendingResults + approvedResults + rejectedResults + withdrawnResults);
            stats.put("pendingPlans", pendingPlans);
            stats.put("planDraft", draftPlans);
            stats.put("planPending", pendingPlans);
            stats.put("planApproved", approvedPlans);
            stats.put("planRejected", rejectedPlans);
            stats.put("planWithdrawn", withdrawnPlans);
            stats.put("resultDraft", draftResults);
            stats.put("resultPending", pendingResults);
            stats.put("resultApproved", approvedResults);
            stats.put("resultRejected", rejectedResults);
            stats.put("resultWithdrawn", withdrawnResults);
        }

        return R.ok(stats);
    }

    private long countByUserAndStatus(Long userId, PlanStatus status) {
        return planService.count(new LambdaQueryWrapper<Plan>()
                .eq(Plan::getUserId, userId)
                .eq(Plan::getStatus, status.getCode()));
    }

    private long countByApproverAndStatus(Long leaderId, PlanStatus status) {
        return planService.count(new LambdaQueryWrapper<Plan>()
                .eq(Plan::getApproveLeaderId, leaderId)
                .eq(Plan::getStatus, status.getCode()));
    }

    private long countResultByUserAndStatus(Long userId, PlanStatus status) {
        return resultService.count(new LambdaQueryWrapper<PlanResult>()
                .eq(PlanResult::getUserId, userId)
                .eq(PlanResult::getStatus, status.getCode()));
    }

    private long countResultByApproverAndStatus(Long leaderId, PlanStatus status) {
        return resultService.count(new LambdaQueryWrapper<PlanResult>()
                .eq(PlanResult::getApproveLeaderId, leaderId)
                .eq(PlanResult::getStatus, status.getCode()));
    }
}
