package com.imooc.user.service;


import com.imooc.user.pojo.Users;
import com.imooc.user.pojo.bo.UserBO;
import org.springframework.web.bind.annotation.*;

/**
 * @描述:
 * @Author
 * @Since 2021/3/27 22:53
 */
@RequestMapping("user-api")
public interface UserService {
    /**
     * 判断用户名时候是存在的
     */
    @GetMapping("user/exixtx")
   public boolean queryUsernameExist(@RequestParam("uername")String uername);

    /**
     * 创建用户
     */
    @PostMapping("user")
    public Users createUser(@RequestBody UserBO userBO);

    /**
     * 检索用户名和密码是否匹配，用于登录
     */
    @GetMapping("verify")
    public Users queryUserForLogin(@RequestParam("uername")String username,
                                   @RequestParam("password")String password);
}
