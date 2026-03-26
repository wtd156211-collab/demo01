package com.wtd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wtd.dto.UserDTO;
import com.wtd.entity.User;

public interface UserService extends IService<User> {
    /**
     * 新增用户
     *
     * @param user
     */
    void addUser(UserDTO user);

    /**
     * 用户登录
     *
     * @param user
     * @return
     */
    String login(UserDTO user);
}
