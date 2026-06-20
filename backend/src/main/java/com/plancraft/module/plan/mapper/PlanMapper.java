package com.plancraft.module.plan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plancraft.module.plan.entity.Plan;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlanMapper extends BaseMapper<Plan> {
}
