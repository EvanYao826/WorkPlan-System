package com.plancraft.module.result.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("result_approval_log")
public class ResultApprovalLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long resultId;
    private Long operatorId;
    private String action;
    private Integer fromStatus;
    private Integer toStatus;
    private String comment;
    private LocalDateTime createTime;
}
