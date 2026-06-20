package com.plancraft.module.result.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plancraft.module.result.entity.PlanResult;
import com.plancraft.module.result.mapper.ResultMapper;
import org.springframework.stereotype.Service;

@Service
public class ResultService extends ServiceImpl<ResultMapper, PlanResult> {
}
