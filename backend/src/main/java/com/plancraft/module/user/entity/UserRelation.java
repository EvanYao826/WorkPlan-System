package com.plancraft.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 汇报关系实体（领导-下属映射）
 */
@Data
@TableName("user_relation")
public class UserRelation {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long leaderId;
    private Long userId;
    private LocalDateTime createTime;
}
