package com.imooc.user.controller;

import com.imooc.controller.BaseController;

import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.pojo.ShopcartBO;
import com.imooc.user.pojo.Users;
import com.imooc.user.pojo.bo.UserBO;
import com.imooc.user.service.UserService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.MD5Utils;
import com.imooc.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;


/**
 * @描述:
 * @Author
 * @Since 2021/3/28 13:53
 */
@Api(value = "注册登录",tags = "用于注册登录的相关接口")
@RestController
@RequestMapping("passport")
public class PassportController extends BaseController {

    @Autowired
    public UserService userService;

    @Autowired
    private RedisOperator redisOperator;
    /**
     * 用户注册
     * @param username
     * @return
     */
    @ApiOperation(value = "用户名是否存在", notes = "用户名是否存在", httpMethod = "GET")
    @GetMapping("/usernameIsExist")
//    public HttpStatus usernameIsExist(@RequestParam String username){
    public IMOOCJSONResult usernameIsExist(@RequestParam String username){
        /** org.apache.commons.lang3.StringUtils 的工具类
         * 还可以额外的去判断一下是不是还是空字符串呢 */
        /**
         * 1：判断用户名是不是是为空
         */
        if (StringUtils.isBlank(username)) {
//            return 500;
//            return HttpStatus.INTERNAL_SERVER_ERROR;
            return IMOOCJSONResult.errorMsg("用户名不能为空");
        }
        /**
         * 2： 查看注册的用户名是不是存在
         */
        boolean isExist = userService.queryUsernameExist(username);
        if (isExist) {
//            return 600;
//            return HttpStatus.INTERNAL_SERVER_ERROR;
            return IMOOCJSONResult.errorMsg("!!!!!");
        }
        /**
         *  3:http请求成功，用户名没有重复
         */
//        return 200;
//        return HttpStatus.OK;
        return IMOOCJSONResult.ok();
    }

    /* 注册  */
    @PostMapping("/regist")
    public IMOOCJSONResult regist(@RequestBody UserBO userBO,
            HttpServletRequest request,
            HttpServletResponse response){
        String username = userBO.getUsername();
        String password = userBO.getPassword();
        String confirmPassword = userBO.getConfirmPassword();

        // 0:判断用户名和密码必须不能为空
        if (StringUtils.isBlank(username) ||
                StringUtils.isBlank(password) ||
                StringUtils.isBlank(confirmPassword)) {
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }
        // 1：查询用户名是不是存在
        boolean isExist = userService.queryUsernameExist(username);
        if (isExist) {
            return IMOOCJSONResult.errorMsg("用户名已经存在");
        }
        // 2：长度不能小于6
        if (password.length() < 6) {
            return IMOOCJSONResult.errorMsg("密码长度不能小于6");
        }
        // 3：判断两次密码是否一致
        if (!password.equals(confirmPassword)) {
            return IMOOCJSONResult.errorMsg("两次密码输入不一致");
        }
        // 4：实现注册
        Users userResult = userService.createUser(userBO);

        userResult = setNullProperty(userResult);


        CookieUtils.setCookie(request,
                response,
                "user",
                JsonUtils.objectToJson(userResult),
                true);  // 加密后前端就看不到值了
        // TODO 生成用户token，存入到redis绘画
//        UsersVO usersVO = convertUserVo(userResult);

        // TODO 同步购物车数据
        synchShopcartData(userResult.getId(), request, response);

        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "用户登录", notes = "用户登录", httpMethod = "POST")
    @PostMapping("/login")
    public IMOOCJSONResult login(@RequestBody UserBO userBO,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception{
        String username = userBO.getUsername();
        String password = userBO.getPassword();

        // 0. 用户名或密码不能为空
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }

        // 1:实现登录
        Users userResult = userService.queryUserForLogin(username,
                MD5Utils.getMD5Str(password));
        if (userResult == null) {
            return IMOOCJSONResult.errorMsg("用户名或密码不正确");
        }
//        userResult = setNullProperty(userResult);
        // 实现redis用户会话
        // TODO
      /***  UsersVO usersVO = convertUserVo(userResult);   */

        /***   CookieUtils.setCookie(request, response,"user",JsonUtils.objectToJson(usersVO),true); */  // 加密后前端就看不到值了

        // TODO 同步购物车数据
        synchShopcartData(userResult.getId(), request, response);
        return IMOOCJSONResult.ok();
    }

    /** 注册登录成功后，同步cookie和redis中的购物车数据 */
    // TODO 建议：放到购物车模块中, 暂时先这样做了
    private void synchShopcartData(String userId,HttpServletRequest request,
                                   HttpServletResponse response) {
        /**
         * 1:redis 中无数据，如果cookie中的购物车为空， 那么这个时候不做任何处理
         *                 如果cookie中的购物车不为空，此时直接放入redis中
         * 2:redis 中有数据，如果cookie中的购物车为空，那么直接把redis的购物车覆盖本地的cookie
         *                 如果cookie中的购物车不为空，
         *                                          如果cookie中某个商品在cookie中存在，
         *                                          则以cookie为主，删除redis中的，
         *                                          把cooike中的商品直接覆盖redis中的（参考京东）
         * 3：同步到redis中以后，覆盖本地cookie购物车的数据，保证本地购物车的数据是同步最新的
         */

        /** 从redis中获取购物车 */
        String shopcartJsonRedis = redisOperator.get(FOODIE_SHOPCART + ":" + userId);

        /** 从cookie中获取购物车 */
        String shopcartStrCookie = CookieUtils.getCookieValue(request, FOODIE_SHOPCART, true);

        if (StringUtils.isBlank(shopcartJsonRedis)) {
            /** redis为空，cookie不为空，直接把cookie中的数据放入到redis中 **/
            if (StringUtils.isNotBlank(shopcartStrCookie)) {
                redisOperator.set(FOODIE_SHOPCART + ":" + userId, shopcartStrCookie);
            }
        }else {
            /** redis 不为空， redis不为空， 合并cookie和redis中购物车的商品数据量（同意商品则）*/
            if (StringUtils.isNotBlank(shopcartStrCookie)) {
                /**
                 * 1: 已经存在的，把cookie中对应的数量，覆盖redis（参考京东）
                 * 2：该商品比较为删除，同意放入一个待删除的list
                 * 3: 从cookie中清除所有待删除的list
                 * 4：合并redis和cookie中的数据
                 * 5：更新到redis和cookie中
                 */
                List<ShopcartBO> shopcartListRedis = JsonUtils.jsonToList(shopcartJsonRedis, ShopcartBO.class);
                List<ShopcartBO> shopcartListCookie = JsonUtils.jsonToList(shopcartStrCookie, ShopcartBO.class);
                /** 定义一个待删除的 list  */
                List<ShopcartBO> pendingDeleteList = new ArrayList<>();

                for (ShopcartBO redisShopcart : shopcartListRedis) {
                    String redisSpecId = redisShopcart.getSpecId();

                    for (ShopcartBO cookieShopcart : shopcartListCookie) {
                        String cookieSpecId = cookieShopcart.getSpecId();

                        if (redisSpecId.equals(cookieSpecId)) {
                            // 覆盖购买数量，不累加，参考京东
                            redisShopcart.setBuyCounts(cookieShopcart.getBuyCounts());
                            // 把cookieShopcart放入到待删除列表，用于最后的删除与合并
                            pendingDeleteList.add(cookieShopcart);
                        }
                    }
                }
                /** 从现有 cookie 中删除对应的覆盖过的商品数据 */
                shopcartListCookie.removeAll(pendingDeleteList);
                /** 合并两个 list */
                shopcartListRedis.addAll(shopcartListCookie);
                /** 更新到 redis 和 cookie */
                CookieUtils.setCookie(request, response, FOODIE_SHOPCART, JsonUtils.objectToJson(shopcartListRedis), true);
                redisOperator.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartListRedis));
            }else {
                /** redis 不为空， cookie为空， 直接把redis覆盖cookie**/
                CookieUtils.setCookie(request, response, FOODIE_SHOPCART, shopcartJsonRedis, true);
            }
        }
    }


    public Users setNullProperty(Users userResult){
        userResult.setUsername(null);
        userResult.setPassword(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);
        return userResult;
    }

    @ApiOperation(value = "用户退出登录", notes = "用户退出登录", httpMethod ="POST")
    @GetMapping("/logout")
    public IMOOCJSONResult lougout(@RequestParam String userId,
    HttpServletRequest request,HttpServletResponse response) {
        // 清除用户相关的信息
        CookieUtils.deleteCookie(request,response,"user");

        // TODO 用户退出登录，需要清除购物车
        redisOperator.del(REDIS_USER_TOKEN + ":" + userId);

        // TODO 分布式会话中需要清除用户数据
        CookieUtils.deleteCookie(request, response, FOODIE_SHOPCART);
        return IMOOCJSONResult.ok();
    }
}
