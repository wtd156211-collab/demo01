package com.wtd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wtd.common.Result;
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

    /**
     * 根据Id获取用户信息
     * @param id
     * @return
     */
    Result<String> getUserById(Long id);

    /**
     * 获取用户分页数据
     * @param pageNum
     * @param pageSize
     * @return  分页数据
     */
    Result<Object> getUserPage(Integer pageNum, Integer pageSize);
}
