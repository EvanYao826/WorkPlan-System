package com.plancraft.module.department.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.plancraft.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("department")
public class Department extends BaseEntity {

    private String name;
    private Long parentId;
    private Long leaderId;
    private Integer sortOrder;
    private Integer status;
}
