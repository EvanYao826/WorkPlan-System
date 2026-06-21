package com.plancraft.module.plan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.plancraft.common.result.R;
import com.plancraft.module.plan.entity.Plan;
import com.plancraft.module.plan.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "计划管理", description = "计划增删改查、提交审批、审批通过/驳回、撤回")
@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    /**
     * 查询计划列表
     * 同事：只能看自己的计划
     * 领导：只看分配给自己审批的计划
     */
    @Operation(summary = "查询计划列表", description = "分页+多条件筛选，员工看自己的，领导看下属的")
    @GetMapping("/list")
    public R<IPage<Plan>> list(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        String role = authentication.getAuthorities().iterator().next().getAuthority();
        Long currentUserId = (Long) authentication.getDetails();

        Long queryUserId = null;
        Long approveLeaderId = null;

        if ("ROLE_LEADER".equals(role)) {
            // 领导：只看分配给自己的
            approveLeaderId = currentUserId;
        } else {
            // 同事：只看自己的
            queryUserId = currentUserId;
        }

        IPage<Plan> result = planService.listPlans(queryUserId, approveLeaderId, type, status, startDate, endDate, title, page, size);
        return R.ok(result);
    }

    /**
     * 查看计划详情
     */
    @Operation(summary = "查看计划详情")
    @GetMapping("/{id}")
    public R<Plan> detail(@PathVariable Long id) {
        Plan plan = planService.getById(id);
        return plan != null ? R.ok(plan) : R.error(404, "计划不存在");
    }

    /**
     * 创建计划
     * 如果传了 approveLeaderId，则创建后直接提交审批（一个事务，失败全部回滚）
     * 如果没传，则保存为草稿
     */
    @Operation(summary = "创建计划", description = "创建草稿或直接提交审批")
    @PostMapping
    public R<Plan> create(@Valid @RequestBody Plan plan, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        Long approveLeaderId = plan.getApproveLeaderId();

        Plan created = planService.createPlan(plan, userId);

        // 如果指定了审批领导，创建后直接提交
        if (approveLeaderId != null) {
            planService.submit(created.getId(), userId, approveLeaderId);
            created = planService.getById(created.getId());
        }

        return R.ok(created);
    }

    /**
     * 提交审批
     */
    @Operation(summary = "提交审批")
    @PutMapping("/{id}/submit")
    public R<Void> submit(@PathVariable Long id,
                          @RequestBody java.util.Map<String, String> body,
                          Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        Long approveLeaderId = body.get("approveLeaderId") != null
                ? Long.valueOf(body.get("approveLeaderId")) : null;
        planService.submit(id, userId, approveLeaderId);
        return R.ok();
    }

    /**
     * 审批通过
     */
    @Operation(summary = "审批通过")
    @PutMapping("/{id}/approve")
    public R<Void> approve(@PathVariable Long id,
                           @RequestBody(required = false) java.util.Map<String, String> body,
                           Authentication authentication) {
        Long leaderId = (Long) authentication.getDetails();
        String comment = body != null ? body.get("comment") : null;
        planService.approve(id, leaderId, comment);
        return R.ok();
    }

    /**
     * 审批驳回
     */
    @Operation(summary = "审批驳回")
    @PutMapping("/{id}/reject")
    public R<Void> reject(@PathVariable Long id,
                          @RequestBody(required = false) java.util.Map<String, String> body,
                          Authentication authentication) {
        Long leaderId = (Long) authentication.getDetails();
        String comment = body != null ? body.get("comment") : null;
        planService.reject(id, leaderId, comment);
        return R.ok();
    }

    /**
     * 撤回
     */
    @Operation(summary = "撤回计划")
    @PutMapping("/{id}/withdraw")
    public R<Void> withdraw(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        planService.withdraw(id, userId);
        return R.ok();
    }
}
