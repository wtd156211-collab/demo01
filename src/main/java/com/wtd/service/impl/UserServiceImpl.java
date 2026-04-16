package com.wtd.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wtd.common.Result;
import com.wtd.common.ResultCode;
import com.wtd.dto.UserDTO;
import com.wtd.entity.User;
import com.wtd.entity.UserInfo;
import com.wtd.mapper.UserInfoMapper;
import com.wtd.mapper.UserMapper;
import com.wtd.service.UserService;
import com.wtd.vo.UserDetailVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String CACHE_KEY_PREFIX = "user:detail:";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

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
    public Result<User> getUserById(Long id) {
        if (id == null) {
            return Result.error(ResultCode.PARAM_ERROR);
        }

        User user = this.getById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        return Result.success(user);
    }

    /**
     * 根据Id更新用户基础信息
     *
     * @param id
     * @param user
     * @return
     */
    @Override
    public Result<String> updateUser(Long id, User user) {
        if (id == null || user == null) {
            return Result.error(ResultCode.PARAM_ERROR);
        }

        User dbUser = this.getById(id);
        if (dbUser == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        user.setId(id);
        this.updateById(user);
        redisTemplate.delete(CACHE_KEY_PREFIX + id);
        return Result.success("更新成功");
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

    /**
     * 查询用户详情，多表联查并使用 Redis 缓存
     *
     * @param userId
     * @return
     */
    @Override
    public Result<UserDetailVO> getUserDetail(Long userId) {
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR);
        }

        String key = CACHE_KEY_PREFIX + userId;

        String json = redisTemplate.opsForValue().get(key);
        if (json != null && !json.isBlank()) {
            try {
                UserDetailVO cacheVO = JSONUtil.toBean(json, UserDetailVO.class);
                return Result.success(cacheVO);
            } catch (Exception e) {
                redisTemplate.delete(key);
            }
        }

        UserDetailVO detail = userInfoMapper.getUserDetail(userId);
        if (detail == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        redisTemplate.opsForValue().set(
                key,
                JSONUtil.toJsonStr(detail),
                10,
                TimeUnit.MINUTES
        );

        return Result.success(detail);
    }

    /**
     * 更新用户扩展信息，并删除用户详情缓存
     *
     * @param userInfo
     * @return
     */
    @Override
    @Transactional
    public Result<String> updateUserInfo(UserInfo userInfo) {
        if (userInfo == null || userInfo.getUserId() == null) {
            return Result.error(ResultCode.PARAM_ERROR);
        }

        User user = this.getById(userInfo.getUserId());
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getUserId, userInfo.getUserId());
        int count = userInfoMapper.update(userInfo, queryWrapper);
        if (count == 0) {
            userInfoMapper.insert(userInfo);
        }

        redisTemplate.delete(CACHE_KEY_PREFIX + userInfo.getUserId());
        return Result.success("更新成功");
    }

    /**
     * 删除用户，并删除用户详情缓存
     *
     * @param userId
     * @return
     */
    @Override
    @Transactional
    public Result<String> deleteUser(Long userId) {
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR);
        }

        User user = this.getById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getUserId, userId);
        userInfoMapper.delete(queryWrapper);

        this.removeById(userId);

        redisTemplate.delete(CACHE_KEY_PREFIX + userId);
        return Result.success("删除成功");
    }
}
