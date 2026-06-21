package com.plancraft.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plancraft.module.user.entity.UserRelation;
import com.plancraft.module.user.mapper.UserRelationMapper;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 汇报关系服务
 * 提供"查领导的所有下属ID"方法（递归，支持多层级）
 */
@Service
public class UserRelationService extends ServiceImpl<UserRelationMapper, UserRelation> {

    /**
     * 获取领导的所有下属ID（递归，包含所有层级）
     * @param leaderId 领导ID
     * @return 下属ID集合（不含 leaderId 自身）
     */
    public List<Long> getSubordinateIds(Long leaderId) {
        Set<Long> result = new LinkedHashSet<>();
        collectSubordinates(leaderId, result);
        return new ArrayList<>(result);
    }

    /**
     * 递归收集下属ID
     */
    private void collectSubordinates(Long leaderId, Set<Long> result) {
        // 查询直接下属
        List<UserRelation> relations = list(new LambdaQueryWrapper<UserRelation>()
                .eq(UserRelation::getLeaderId, leaderId)
                .select(UserRelation::getUserId));

        for (UserRelation rel : relations) {
            if (result.add(rel.getUserId())) {
                // 递归查下属的下属
                collectSubordinates(rel.getUserId(), result);
            }
        }
    }
}
