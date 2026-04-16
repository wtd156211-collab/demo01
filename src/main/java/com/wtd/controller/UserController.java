package com.wtd.controller;

import com.wtd.common.Result;
import com.wtd.dto.UserDTO;
import com.wtd.entity.User;
import com.wtd.entity.UserInfo;
import com.wtd.service.UserService;
import com.wtd.vo.UserDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户信息
     * @return
     */
    @GetMapping("/{id}")
    public Result<User> getUserInfo(@PathVariable("id") Long id) {
        log.info("正在查询ID为" + id + "的用户信息");
        return userService.getUserById(id);
    }
    /**
     * 新增用户
     * @return
     */
    @PostMapping
    public Result<String> addUser(@RequestBody UserDTO user) {
        log.info("接收到用户信息:" + user.getUsername() + " " + user.getPassword());
        userService.addUser(user);
        String data="新增成功,接收到用户信息:" + user.getUsername() + " " + user.getPassword();
        return Result.success(data);
    }
    /**
     * 全量更新用户信息
     * @return
     */
     @PutMapping("/{id}")
    public Result<String> updateUser(@PathVariable Long id, @RequestBody User user) {
         log.info("正在更新ID为" + id + "的用户信息");
         return userService.updateUser(id, user);
    }
    /**
     * 用户登录
     * @return
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody UserDTO user) {
        log.info("接收到用户登录信息:" + user.getUsername() + " " + user.getPassword());
        String token = userService.login(user);
        return Result.success(token);
    }

    /**
     * 获取用户分页数据
     * @return
     */
    @GetMapping("/page")
    public Result<Object> getUserPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "5") Integer pageSize) {
        log.info("正在查询第" + pageNum + "页,每页" + pageSize + "条数据");
        return userService.getUserPage(pageNum, pageSize);
    }

    /**
     * 查询用户详情（多表联查 + Redis）
     * @return
     */
    @GetMapping("/{id}/detail")
    public Result<UserDetailVO> getUserDetail(@PathVariable("id") Long userId) {
        return userService.getUserDetail(userId);
    }

    /**
     * 更新用户扩展信息
     * @return
     */
    @PutMapping("/{id}/detail")
    public Result<String> updateUserInfo(@PathVariable("id") Long userId,
                                         @RequestBody UserInfo userInfo) {
        if (userInfo == null) {
            userInfo = new UserInfo();
        }
        userInfo.setUserId(userId);
        return userService.updateUserInfo(userInfo);
    }

    /**
     * 删除用户
     * @return
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUserById(@PathVariable("id") Long userId) {
        return userService.deleteUser(userId);
    }
}
