package com.wtd.controller;

import com.wtd.common.Result;
import com.wtd.dto.UserDTO;
import com.wtd.entity.User;
import com.wtd.service.UserService;
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
    public Result<String> getUserInfo(@PathVariable("id") Long id) {
        log.info("正在查询ID为" + id + "的用户信息");
        String data="获取成功，正在查询ID为" + id + "的用户信息";
        return Result.success(data);
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
         String data="更新成功,ID为" + id + "的用户信息已更新为:" + user.toString();
        return Result.success(data);
    }
    /**
     * 删除用户信息
     * @return
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        int i = 1 / 0;
        log.info("测试异常处理");
        log.info("正在删除ID为" + id + "的用户信息");
        String data="删除成功,ID为" + id + "的用户信息已删除";
        return Result.success(data);
    }
    /**
     * 用户登录
     * @return
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody UserDTO user) {
        log.info("接收到用户登录信息:" + user.getUsername() + " " + user.getPassword());
        String data="登录成功,接收到用户登录信息:" + user.getUsername() + " " + user.getPassword();
        userService.login(user);
        return Result.success(data);
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
}