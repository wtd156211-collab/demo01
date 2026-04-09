package com.wtd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wtd.common.Result;
import com.wtd.common.ResultCode;
import com.wtd.dto.UserDTO;
import com.wtd.entity.User;
import com.wtd.mapper.UserMapper;
import com.wtd.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 新增用户
     *
     * @param user
     */
    public void addUser(UserDTO user) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, user.getUsername());
        User existUser = this.getOne(queryWrapper);

        if (existUser != null) {
            throw new RuntimeException(ResultCode.USER_HAS_EXISTED.getMsg());
        }

        User u = new User();
        BeanUtils.copyProperties(user, u);
        this.save(u);
    }

    /**
     * 用户登录
     *
     * @param user
     * @return
     */
    @Override
    public String login(UserDTO user) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, user.getUsername());
        User dbUser = this.getOne(queryWrapper);

        if (dbUser == null) {
            throw new RuntimeException(ResultCode.USER_NOT_EXIST.getMsg());
        }

        if (!dbUser.getPassword().equals(user.getPassword())) {
            throw new RuntimeException(ResultCode.PASSWORD_ERROR.getMsg());
        }

        return "token-" + user.getUsername() + "-" + System.currentTimeMillis();
    }

    /**
     * 根据Id获取用户信息
     *
     * @param id
     * @return
     */
    @Override
    public Result<String> getUserById(Long id) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, id);
        User user = this.getOne(queryWrapper);
        return Result.success(user.toString());
    }

    /**
     * 获取用户分页数据
     *
     * @param pageNum
     * @param pageSize
     * @return 分页数据
     */
    @Override
    public Result<Object> getUserPage(Integer pageNum, Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> userPage = userMapper.selectPage(page, null);
        return Result.success(userPage);
    }
}
