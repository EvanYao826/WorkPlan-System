package com.plancraft.module.plan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.plancraft.common.result.R;
import com.plancraft.module.plan.entity.Plan;
import com.plancraft.module.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    /**
     * 查询计划列表（分页 + 多条件筛选）
     */
    @GetMapping("/list")
    public R<IPage<Plan>> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        // 员工只能看自己的，领导可以看所有（后续用数据权限拦截器替代）
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        Long queryUserId = userId;
        if (!"ROLE_LEADER".equals(role)) {
            queryUserId = (Long) authentication.getDetails();
        }

        IPage<Plan> result = planService.listPlans(queryUserId, type, status, startDate, endDate, title, page, size);
        return R.ok(result);
    }

    /**
     * 查看计划详情
     */
    @GetMapping("/{id}")
    public R<Plan> detail(@PathVariable Long id) {
        Plan plan = planService.getById(id);
        return plan != null ? R.ok(plan) : R.error(404, "计划不存在");
    }
}
