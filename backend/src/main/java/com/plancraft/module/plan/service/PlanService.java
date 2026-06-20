package com.plancraft.module.plan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plancraft.module.plan.entity.Plan;
import com.plancraft.module.plan.mapper.PlanMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Service
public class PlanService extends ServiceImpl<PlanMapper, Plan> {

    /**
     * 分页查询计划列表
     */
    public IPage<Plan> listPlans(Long userId, Integer type, Integer status,
                                 LocalDate startDate, LocalDate endDate,
                                 String title, int page, int size) {
        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, Plan::getUserId, userId);
        wrapper.eq(type != null, Plan::getType, type);
        wrapper.eq(status != null, Plan::getStatus, status);
        wrapper.ge(startDate != null, Plan::getPlanDate, startDate);
        wrapper.le(endDate != null, Plan::getPlanDate, endDate);
        wrapper.like(StringUtils.hasText(title), Plan::getTitle, title);
        wrapper.orderByDesc(Plan::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }
}
