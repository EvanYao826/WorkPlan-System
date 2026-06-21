package com.plancraft.module.result.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.plancraft.common.result.R;
import com.plancraft.module.result.entity.PlanResult;
import com.plancraft.module.result.service.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "成果管理", description = "成果增删改查、提交审批、审批通过/驳回")
@RestController
@RequestMapping("/api/result")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    /**
     * 查询成果列表
     * 同事：看自己的
     * 领导：看分配给自己的
     */
    @GetMapping("/list")
    public R<IPage<PlanResult>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        String role = authentication.getAuthorities().iterator().next().getAuthority();
        Long currentUserId = (Long) authentication.getDetails();

        Long queryUserId = null;
        Long approveLeaderId = null;

        if ("ROLE_LEADER".equals(role)) {
            approveLeaderId = currentUserId;
        } else {
            queryUserId = currentUserId;
        }

        IPage<PlanResult> result = resultService.listResults(queryUserId, approveLeaderId, status, page, size);
        return R.ok(result);
    }

    /**
     * 查看成果详情
     */
    @GetMapping("/{id}")
    public R<PlanResult> detail(@PathVariable Long id) {
        PlanResult result = resultService.getById(id);
        return result != null ? R.ok(result) : R.error(404, "成果不存在");
    }

    /**
     * 创建成果（草稿或直接提交）
     * 如果传了 approveLeaderId，创建后直接提交审批
     */
    @PostMapping
    public R<PlanResult> create(@Valid @RequestBody PlanResult result, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        Long approveLeaderId = result.getApproveLeaderId();

        PlanResult created = resultService.createResult(result, userId);

        if (approveLeaderId != null) {
            resultService.submit(created.getId(), userId, approveLeaderId);
            created = resultService.getById(created.getId());
        }

        return R.ok(created);
    }

    /**
     * 审批通过
     */
    @PutMapping("/{id}/approve")
    public R<Void> approve(@PathVariable Long id,
                           @RequestBody(required = false) java.util.Map<String, String> body,
                           Authentication authentication) {
        Long leaderId = (Long) authentication.getDetails();
        String comment = body != null ? body.get("comment") : null;
        resultService.approve(id, leaderId, comment);
        return R.ok();
    }

    /**
     * 审批驳回
     */
    @PutMapping("/{id}/reject")
    public R<Void> reject(@PathVariable Long id,
                          @RequestBody(required = false) java.util.Map<String, String> body,
                          Authentication authentication) {
        Long leaderId = (Long) authentication.getDetails();
        String comment = body != null ? body.get("comment") : null;
        resultService.reject(id, leaderId, comment);
        return R.ok();
    }

    /**
     * 撤回
     */
    @PutMapping("/{id}/withdraw")
    public R<Void> withdraw(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        resultService.withdraw(id, userId);
        return R.ok();
    }
}
