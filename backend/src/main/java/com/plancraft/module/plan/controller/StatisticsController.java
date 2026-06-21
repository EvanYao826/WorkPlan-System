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
    @GetMapping
    public R<Map<String, Object>> statistics(Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        Map<String, Object> stats = new LinkedHashMap<>();

        if ("ROLE_LEADER".equals(role)) {
            // 领导：待审批数量
            long pendingPlans = planService.count(
                    new LambdaQueryWrapper<Plan>()
                            .eq(Plan::getApproveLeaderId, userId)
                            .eq(Plan::getStatus, PlanStatus.PENDING.getCode()));
            long pendingResults = resultService.count(
                    new LambdaQueryWrapper<PlanResult>()
                            .eq(PlanResult::getApproveLeaderId, userId)
                            .eq(PlanResult::getStatus, PlanStatus.PENDING.getCode()));
            stats.put("pendingPlans", pendingPlans);
            stats.put("pendingResults", pendingResults);
            stats.put("pendingTotal", pendingPlans + pendingResults);
        } else {
            // 同事：我的计划数、我的成果数
            long myPlans = planService.count(
                    new LambdaQueryWrapper<Plan>().eq(Plan::getUserId, userId));
            long myResults = resultService.count(
                    new LambdaQueryWrapper<PlanResult>().eq(PlanResult::getUserId, userId));
            long pendingPlans = planService.count(
                    new LambdaQueryWrapper<Plan>()
                            .eq(Plan::getUserId, userId)
                            .eq(Plan::getStatus, PlanStatus.PENDING.getCode()));
            stats.put("myPlans", myPlans);
            stats.put("myResults", myResults);
            stats.put("pendingPlans", pendingPlans);
        }

        return R.ok(stats);
    }
}
