package com.plancraft.module.department.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plancraft.common.exception.BusinessException;
import com.plancraft.module.department.entity.Department;
import com.plancraft.module.department.mapper.DepartmentMapper;
import com.plancraft.module.user.entity.User;
import com.plancraft.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService extends ServiceImpl<DepartmentMapper, Department> {

    private final UserService userService;

    /**
     * 获取部门树
     */
    public List<Map<String, Object>> getTree() {
        List<Department> all = list(new LambdaQueryWrapper<Department>()
                .orderByAsc(Department::getSortOrder));
        return buildTree(all, 0L);
    }

    /**
     * 新增部门
     */
    @Transactional
    public Department create(Department dept) {
        // 校验部门名称不重复（同级下）
        long count = count(new LambdaQueryWrapper<Department>()
                .eq(Department::getParentId, dept.getParentId() != null ? dept.getParentId() : 0)
                .eq(Department::getName, dept.getName()));
        if (count > 0) {
            throw new BusinessException("同级下已存在同名部门");
        }

        if (dept.getParentId() == null) {
            dept.setParentId(0L);
        }
        if (dept.getSortOrder() == null) {
            dept.setSortOrder(0);
        }
        if (dept.getStatus() == null) {
            dept.setStatus(1);
        }
        dept.setCreateTime(LocalDateTime.now());
        dept.setUpdateTime(LocalDateTime.now());
        dept.setVersion(1);
        save(dept);
        return dept;
    }

    /**
     * 编辑部门
     */
    @Transactional
    public Department updateDept(Long id, Department dept) {
        Department existing = getByIdOrThrow(id);

        // 如果改了名称，校验同级不重复
        if (dept.getName() != null && !dept.getName().equals(existing.getName())) {
            Long parentId = dept.getParentId() != null ? dept.getParentId() : existing.getParentId();
            long count = count(new LambdaQueryWrapper<Department>()
                    .eq(Department::getParentId, parentId)
                    .eq(Department::getName, dept.getName())
                    .ne(Department::getId, id));
            if (count > 0) {
                throw new BusinessException("同级下已存在同名部门");
            }
            existing.setName(dept.getName());
        }

        if (dept.getParentId() != null) {
            // 不能把自己设为上级
            if (dept.getParentId().equals(id)) {
                throw new BusinessException("上级部门不能是自己");
            }
            existing.setParentId(dept.getParentId());
        }
        if (dept.getLeaderId() != null) {
            existing.setLeaderId(dept.getLeaderId());
        }
        if (dept.getSortOrder() != null) {
            existing.setSortOrder(dept.getSortOrder());
        }
        if (dept.getStatus() != null) {
            existing.setStatus(dept.getStatus());
        }
        existing.setUpdateTime(LocalDateTime.now());
        updateById(existing);
        return existing;
    }

    /**
     * 删除部门
     */
    @Transactional
    public void deleteDept(Long id) {
        getByIdOrThrow(id);

        // 校验是否有子部门
        long childCount = count(new LambdaQueryWrapper<Department>()
                .eq(Department::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("该部门下有子部门，不能删除");
        }

        // 校验是否有关联用户
        long userCount = userService.count(new LambdaQueryWrapper<User>()
                .eq(User::getDepartmentId, id));
        if (userCount > 0) {
            throw new BusinessException("该部门下有用户，不能删除");
        }

        removeById(id);
    }

    // --- 内部方法 ---

    private List<Map<String, Object>> buildTree(List<Department> all, Long parentId) {
        return all.stream()
                .filter(d -> Objects.equals(d.getParentId(), parentId))
                .map(d -> {
                    Map<String, Object> node = new LinkedHashMap<>();
                    node.put("id", d.getId().toString());
                    node.put("name", d.getName());
                    node.put("parentId", d.getParentId() == 0 ? null : d.getParentId().toString());
                    node.put("leaderId", d.getLeaderId());
                    node.put("sortOrder", d.getSortOrder());
                    node.put("status", d.getStatus());
                    node.put("children", buildTree(all, d.getId()));
                    return node;
                })
                .collect(Collectors.toList());
    }

    private Department getByIdOrThrow(Long id) {
        Department dept = getById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        return dept;
    }
}
