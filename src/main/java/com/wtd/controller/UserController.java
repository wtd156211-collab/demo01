package com.wtd.controller;

import com.wtd.common.Result;
import com.wtd.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * 获取用户信息
     * @return
     */
    @GetMapping("/{id}")
    public Result getUserInfo(@PathVariable("id") Long id) {
        return new Result("200", "获取成功，正在查询ID为" + id + "的用户信息");
    }
    /**
     * 新增用户
     * @return
     */
    @PostMapping
    public Result addUser(@RequestBody User user) {
        return new Result("200", "新增成功,接收到用户信息:" + user.toString());
    }
    /**
     * 全量更新用户信息
     * @return
     */
     @PutMapping("/{id}")
    public Result updateUser(@PathVariable Long id, @RequestBody User user) {
        return new Result("200", "更新成功,ID为" + id + "的用户信息已更新为:" + user.toString());
    }
    /**
     * 删除用户信息
     * @return
     */
    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable Long id) {
        int i = 1 / 0;
        log.info("测试异常处理");
        return new Result("200", "删除成功,ID为" + id + "的用户信息已删除");
    }
}
