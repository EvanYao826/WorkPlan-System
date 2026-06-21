package com.plancraft.module.department.controller;

import com.plancraft.common.result.R;
import com.plancraft.module.department.entity.Department;
import com.plancraft.module.department.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "部门管理", description = "部门树增删改查")
@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 获取部门树
     */
    @GetMapping("/tree")
    public R<List<Map<String, Object>>> tree() {
        return R.ok(departmentService.getTree());
    }

    /**
     * 新增部门
     */
    @PostMapping
    public R<Department> create(@Valid @RequestBody Department dept) {
        return R.ok(departmentService.create(dept));
    }

    /**
     * 编辑部门
     */
    @PutMapping("/{id}")
    public R<Department> update(@PathVariable Long id, @RequestBody Department dept) {
        return R.ok(departmentService.updateDept(id, dept));
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        departmentService.deleteDept(id);
        return R.ok();
    }
}
