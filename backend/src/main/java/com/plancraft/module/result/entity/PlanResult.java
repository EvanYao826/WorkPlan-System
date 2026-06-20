package com.plancraft.module.result.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.plancraft.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("result")
public class PlanResult extends BaseEntity {

    private Long planId;
    private Long userId;
    private String title;
    private String content;
    private String attachmentUrl;
    private Integer status;
    private Integer rejectCount;
    private LocalDateTime submitTime;
    private Long approveLeaderId;
    private LocalDateTime approveTime;
    private String approveComment;

    @TableLogic
    private Integer deleted;
}
