package com.plancraft.module.plan.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.plancraft.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plan")
public class Plan extends BaseEntity {

    private Long userId;
    private Integer type;
    private String title;
    private String content;
    private LocalDate planDate;
    private Integer status;
    private Integer rejectCount;
    private LocalDateTime submitTime;
    private Long approveLeaderId;
    private LocalDateTime approveTime;
    private String approveComment;

    @TableLogic
    private Integer deleted;
}
