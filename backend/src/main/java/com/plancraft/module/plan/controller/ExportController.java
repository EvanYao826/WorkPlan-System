package com.plancraft.module.plan.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plancraft.module.plan.entity.Plan;
import com.plancraft.module.plan.service.PlanService;
import com.plancraft.module.result.entity.PlanResult;
import com.plancraft.module.result.service.ResultService;
import com.plancraft.module.user.entity.UserRelation;
import com.plancraft.module.user.service.UserRelationService;
import com.plancraft.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "数据导出", description = "导出计划/成果为 Excel")
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final PlanService planService;
    private final ResultService resultService;
    private final UserRelationService userRelationService;
    private final UserService userService;

    @Operation(summary = "导出计划列表", description = "领导导出下属计划，员工导出自己的计划")
    @GetMapping("/plans")
    public void exportPlans(HttpServletResponse response, Authentication authentication) throws Exception {
        Long userId = (Long) authentication.getDetails();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // 查询数据
        List<Plan> plans;
        if ("ROLE_LEADER".equals(role)) {
            List<Long> subordinateIds = userRelationService.getSubordinateIds(userId);
            subordinateIds.add(userId); // 包含自己
            plans = planService.list(new LambdaQueryWrapper<Plan>()
                    .in(Plan::getUserId, subordinateIds)
                    .orderByDesc(Plan::getCreateTime));
        } else {
            plans = planService.list(new LambdaQueryWrapper<Plan>()
                    .eq(Plan::getUserId, userId)
                    .orderByDesc(Plan::getCreateTime));
        }

        // 转换为导出对象
        List<PlanExportVO> exportData = plans.stream().map(p -> {
            PlanExportVO vo = new PlanExportVO();
            vo.setId(p.getId().toString());
            vo.setTitle(p.getTitle());
            vo.setType(p.getType() == 1 ? "日计划" : "月计划");
            vo.setPlanDate(p.getPlanDate() != null ? p.getPlanDate().toString() : "");
            vo.setStatus(statusLabel(p.getStatus()));
            vo.setContent(p.getContent());
            vo.setCreateTime(p.getCreateTime() != null ? p.getCreateTime().toString() : "");
            return vo;
        }).collect(Collectors.toList());

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("计划列表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 写入 Excel
        EasyExcel.write(response.getOutputStream(), PlanExportVO.class).sheet("计划列表").doWrite(exportData);
    }

    @Operation(summary = "导出成果列表", description = "领导导出下属成果，员工导出自己的成果")
    @GetMapping("/results")
    public void exportResults(HttpServletResponse response, Authentication authentication) throws Exception {
        Long userId = (Long) authentication.getDetails();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // 查询数据
        List<PlanResult> results;
        if ("ROLE_LEADER".equals(role)) {
            List<Long> subordinateIds = userRelationService.getSubordinateIds(userId);
            subordinateIds.add(userId);
            results = resultService.list(new LambdaQueryWrapper<PlanResult>()
                    .in(PlanResult::getUserId, subordinateIds)
                    .orderByDesc(PlanResult::getCreateTime));
        } else {
            results = resultService.list(new LambdaQueryWrapper<PlanResult>()
                    .eq(PlanResult::getUserId, userId)
                    .orderByDesc(PlanResult::getCreateTime));
        }

        // 转换为导出对象
        List<ResultExportVO> exportData = results.stream().map(r -> {
            ResultExportVO vo = new ResultExportVO();
            vo.setId(r.getId().toString());
            vo.setTitle(r.getTitle());
            vo.setPlanId(r.getPlanId().toString());
            vo.setStatus(statusLabel(r.getStatus()));
            vo.setContent(r.getContent());
            vo.setCreateTime(r.getCreateTime() != null ? r.getCreateTime().toString() : "");
            return vo;
        }).collect(Collectors.toList());

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("成果列表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 写入 Excel
        EasyExcel.write(response.getOutputStream(), ResultExportVO.class).sheet("成果列表").doWrite(exportData);
    }

    private String statusLabel(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "待审批";
            case 2 -> "已通过";
            case 3 -> "已驳回";
            case 4 -> "已撤回";
            default -> "未知";
        };
    }

    // --- 导出 VO ---

    @Data
    static class PlanExportVO {
        @ExcelProperty("ID")
        @ColumnWidth(20)
        private String id;

        @ExcelProperty("计划标题")
        @ColumnWidth(30)
        private String title;

        @ExcelProperty("类型")
        @ColumnWidth(10)
        private String type;

        @ExcelProperty("计划日期")
        @ColumnWidth(15)
        private String planDate;

        @ExcelProperty("状态")
        @ColumnWidth(10)
        private String status;

        @ExcelProperty("计划内容")
        @ColumnWidth(50)
        private String content;

        @ExcelProperty("创建时间")
        @ColumnWidth(20)
        private String createTime;
    }

    @Data
    static class ResultExportVO {
        @ExcelProperty("ID")
        @ColumnWidth(20)
        private String id;

        @ExcelProperty("成果标题")
        @ColumnWidth(30)
        private String title;

        @ExcelProperty("关联计划ID")
        @ColumnWidth(20)
        private String planId;

        @ExcelProperty("状态")
        @ColumnWidth(10)
        private String status;

        @ExcelProperty("成果内容")
        @ColumnWidth(50)
        private String content;

        @ExcelProperty("创建时间")
        @ColumnWidth(20)
        private String createTime;
    }
}
