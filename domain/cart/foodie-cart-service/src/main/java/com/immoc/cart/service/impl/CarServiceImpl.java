package com.immoc.cart.service.impl;

import com.imooc.cart.service.CartService;
import com.imooc.pojo.ShopcartBO;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.imooc.controller.BaseController.FOODIE_SHOPCART;

@RestController
@Slf4j
public class CarServiceImpl implements CartService {
    @Autowired
    private RedisOperator redisOperator;

    @Override
    public boolean addItemToCart(@RequestParam("userId") String userId,
                                 @RequestBody ShopcartBO shopcartBO) {
        /**
         * 前端用户再登录的时候，添加商品到购物车，会同时再后端同步购物车到redis缓存
         * 需要判断当前购物车中包含的存在的商品，如果存在则累加购物车
         * */
        String shopcartJson = redisOperator.get(FOODIE_SHOPCART + ":" + userId);
        List<ShopcartBO> shopcartList = null;
        if (StringUtils.isBlank(shopcartJson)) {
            /** redis中已经有购物车了 **/
            JsonUtils.jsonToList(shopcartJson, ShopcartBO.class);
            /** 判断购物车中是否已经存在已有商品 **/
            boolean isHaving = false;
            for ( ShopcartBO sc : shopcartList) {
                String tmpSpecId = sc.getSpecId();
                if (tmpSpecId.equals(shopcartBO.getSpecId())) {
                    sc.setBuyCounts(sc.getBuyCounts() + shopcartBO.getBuyCounts());
                    isHaving = true;
                }
                if (!isHaving) {
                    shopcartList.add(shopcartBO);
                }
            }
        }else {
            /** redis 中没有购物车 **/
            shopcartList = new ArrayList<>();
            /** 直接添加到购物车 **/
            shopcartList.add(shopcartBO);
        }

        /** 重新把购物车的值放进去，覆盖redis中的购物车 **/
        redisOperator.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartList));
        return true;

    }

    @Override
    public boolean removeItemFromCart(@RequestParam("userID") String userId,
                                      @RequestParam("itemSpecId") String itemSpecId) {
        String shopcarJson = redisOperator.get(FOODIE_SHOPCART);
        if (StringUtils.isNotBlank(shopcarJson)) {
            // redis 中已经存在购物车了
            List<ShopcartBO> shopcartList = JsonUtils.jsonToList(shopcarJson, ShopcartBO.class);
            // 判断购车中是否已经存在 已有商品，如果有真的话择删除
            for (ShopcartBO sc : shopcartList) {
                String tempSpecId = sc.getSpecId();
                if (tempSpecId.equals(itemSpecId)) {
                    shopcartList.remove(sc);
                    break;
                }
            }
            // 覆盖现有redis中的购物车
            redisOperator.set(FOODIE_SHOPCART + ":" + userId , JsonUtils.objectToJson(shopcartList));
        }
        return true;
    }

    @Override
    public boolean clearCart(String userId) {
        redisOperator.del(FOODIE_SHOPCART + ":" + userId);
        return true;
    }
}
