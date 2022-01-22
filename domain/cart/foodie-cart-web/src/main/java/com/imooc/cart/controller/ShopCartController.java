package com.imooc.cart.controller;

import com.imooc.cart.service.CartService;
import com.imooc.pojo.ShopcartBO;
import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
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



import static com.imooc.controller.BaseController.FOODIE_SHOPCART;

/**
 * @描述:
 * @Author
 * @Since 2021/3/27 18:05
 */
@Api(value = "购物车接口Controller" ,tags = {"购物车接口Controller"})
@RestController
@RequestMapping("shopcart")
@SuppressWarnings("all")
public class ShopCartController {


   @Autowired
   private RedisOperator redisOperator;

   @Autowired
   private CartService cartService;

    @ApiOperation(value = "添加商品到购物车", notes = "添加商品到购物车", httpMethod = "POST")
    @GetMapping("/add")
    public IMOOCJSONResult add(
            @RequestParam String userId,
            @RequestBody ShopcartBO shopcartBO,
            HttpServletRequest request,
            HttpServletResponse response){
       if (StringUtils.isBlank(userId)) {
           return IMOOCJSONResult.errorMsg("");
       }
        System.out.println(shopcartBO);
       // TODO 前端用在登录的情况下，添加商品在购物车，会同时在后端同步到redis购物车
       cartService.addItemToCart(userId,shopcartBO);
       return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "从购物车中删除商品", notes = "从购物车中删除商品", httpMethod = "POST")
    @GetMapping("/del")
    public IMOOCJSONResult del(
            @RequestParam String userId,
            @RequestParam String itemSpecId,
            HttpServletRequest request,
            HttpServletResponse response){
        if (StringUtils.isBlank(userId)) {
            return IMOOCJSONResult.errorMsg("参数不能为空");
        }

        // TODO 用户在页面删除购物车中的商品数据，如果此时用户已经登录，则需要同步删除购物车中的商品数据
        cartService.removeItemFromCart(userId, itemSpecId);
        return IMOOCJSONResult.ok();
    }

    // TODO 1)购物车清空功能
//          2) 加减号 - 添加，减少商品数量
//           +1 -1 -1 = 0 =》 -1 -1 +1 = 1(问题：如何保证前端请求按照顺序执行)
    

}
