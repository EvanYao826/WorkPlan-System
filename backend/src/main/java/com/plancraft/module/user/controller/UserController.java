package com.plancraft.module.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plancraft.common.result.R;
import com.plancraft.module.user.entity.User;
import com.plancraft.module.user.enums.Role;
import com.plancraft.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 查询所有领导（用于员工提交计划时选择审批人）
     */
    @GetMapping("/leaders")
    public R<List<Map<String, Object>>> listLeaders() {
        List<User> leaders = userService.list(
                new LambdaQueryWrapper<User>()
                        .eq(User::getRole, Role.LEADER.getCode())
                        .eq(User::getStatus, 1)
                        .select(User::getId, User::getName, User::getUsername, User::getDepartmentId)
        );
        List<Map<String, Object>> result = leaders.stream()
                .<Map<String, Object>>map(u -> {
                    var map = new java.util.LinkedHashMap<String, Object>();
                    map.put("id", u.getId().toString());
                    map.put("name", u.getName());
                    map.put("username", u.getUsername());
                    map.put("departmentId", u.getDepartmentId());
                    return map;
                })
                .toList();
        return R.ok(result);
    }
}
