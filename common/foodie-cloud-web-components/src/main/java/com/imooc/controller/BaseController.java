package com.imooc.controller;

import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * @描述:
 * @Author
 * @Since 2021/3/27 18:05
 */
//@ApiIgnore
@RestController
public class BaseController {

    public static final String FOODIE_SHOPCART = "shopcart";

    /** Ctrl + Shift + U 大小写转化 */
    public static final Integer COMMON_PAGE_SIZE = 10;
    public static final Integer PAGE_SIZE = 20;

    public static final String REDIS_USER_TOKEN = "redis_user_token";

//    @Autowired
//    private RedisOperator redisOperator;

    /** 支付中心的调用地址 ，是部署在生产环境上的项目 foodie-payment*/
    public String paymentUrl = "http://payment.t.mukewang.com/foodie-payment/payment/createMerchantOrder";		// produce

    /**
     * 微信支付成功，通知到支付中心，再通知天天吃货平台
     * 微信支付成功 -> 支付中心 -> 平台
     *                       -> 回调通知的url
     */
    public String payReturnUrl = "http://api.z.mukewang.com:8088/foodie-dev-api/orders/notifyMerchantOrderPaid";

    /* 用户上传的头像地址 */
//    public static final String IMAGE_USER_FACE_LOCATION = "D:\\Java\\ideaIU\\workspaces2\\imooc_prictice\\foodie-dev\\images";
    /** 为了在服务器上兼容，使用File.separator代替斜杠 */
    public static final
    String IMAGE_USER_FACE_LOCATION =
           "D:" + File.separator +
           "Java" + File.separator +
           "ideaIU" + File.separator +
           "workspaces2" + File.separator +
           "imooc_prictice" + File.separator +
           "foodie-dev" + File.separator +
            "images";


//    public UsersVO convertUserVo(Users user) {
        // TODO 生成用户token，存入到redis会话
        /** 实现用户 redis 会话 ， 使用 token 生成*/
        /**
         *  使用 UUID 设置token
         *  token = UUID +  用户的id  然后设置到 redis中去就行
         *
         *  然后也要放到前端的 cookie 中去：
         *  1:直接写入： 两部分： 设置用户的信息cooikie的信息 CookieUtils.setCookie(request, response,"user",JsonUtils.objectToJson(userResult),true);
         *              再起名，userCookie
         *  2：把 uniquetToken + userResult 封装到一起，作为一个整体也可以
         *
         *  这里我们使用第二种方式
         */
//        String uniquetToken = UUID.randomUUID().toString().trim();
//        redisOperator.set(REDIS_USER_TOKEN + ":" + user.getId(), uniquetToken);
//
//        // 这里我们使用第二种方式 uniquetToken + userResult  封装到一起，作为一个整体也可以
//        UsersVO usersVO = new UsersVO();
//        BeanUtils.copyProperties(user, usersVO);
//        usersVO.setUserUniqueToken(uniquetToken);
//
//
//        return usersVO;
//    }



}
