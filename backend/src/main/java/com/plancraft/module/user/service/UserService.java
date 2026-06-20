package com.plancraft.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.plancraft.module.user.entity.User;

public interface UserService extends IService<User> {

    /**
     * 按用户名查询用户
     */
    User getByUsername(String username);
}
