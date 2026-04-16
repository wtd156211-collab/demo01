package com.wtd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wtd.common.Result;
import com.wtd.dto.UserDTO;
import com.wtd.entity.User;
import com.wtd.entity.UserInfo;
import com.wtd.vo.UserDetailVO;

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
    Result<User> getUserById(Long id);

    /**
     * 根据Id更新用户基础信息
     * @param id
     * @param user
     * @return
     */
    Result<String> updateUser(Long id, User user);

    /**
     * 获取用户分页数据
     * @param pageNum
     * @param pageSize
     * @return  分页数据
     */
    Result<Object> getUserPage(Integer pageNum, Integer pageSize);

    /**
     * 查询用户详情，多表联查并使用 Redis 缓存
     * @param userId
     * @return
     */
    Result<UserDetailVO> getUserDetail(Long userId);

    /**
     * 更新用户扩展信息，并删除用户详情缓存
     * @param userInfo
     * @return
     */
    Result<String> updateUserInfo(UserInfo userInfo);

    /**
     * 删除用户，并删除用户详情缓存
     * @param userId
     * @return
     */
    Result<String> deleteUser(Long userId);
}
