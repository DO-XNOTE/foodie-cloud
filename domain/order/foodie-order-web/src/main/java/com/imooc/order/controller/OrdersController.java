package com.imooc.order.controller;

import com.imooc.controller.BaseController;
import com.imooc.enums.OrderStatusEnum;
import com.imooc.enums.PayMethod;

import com.imooc.order.pojo.OrderStatus;
import com.imooc.order.pojo.bo.center.PlaceOrderBO;
import com.imooc.order.pojo.bo.center.SubmitOrderBO;
import com.imooc.order.pojo.vo.MerchantOrdersVO;
import com.imooc.order.pojo.vo.OrderVO;
import com.imooc.order.service.OrderService;
import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.pojo.ShopcartBO;
import com.imooc.utils.CookieUtils;

import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @描述:
 * @Author
 * @Since 2021/3/27 18:05
 */
@Api(value = "订单相关", tags = "订单相关相关的api接口")
@RestController
@RequestMapping("orders")
public class OrdersController extends BaseController {

    @Autowired
    private OrderService ordersService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisOperator redisOperator;

    @Autowired
    private RedissonClient redissonClient;

    @ApiOperation(value = "获取Token实现幂等性", notes = "获取Token实现幂等性", httpMethod = "POST")
    @PostMapping("/getOrderToken")
    public IMOOCJSONResult getOrderToken(HttpSession session) {
        String token = UUID.randomUUID().toString().trim();
        /**
         * 分布式的情况，当前用户在两个浏览器上（chrome和火狐）上都登录，同时下订单，是可以的
         * 防止一个用户在一个浏览器的当前的session中重复下单
         */
        // redis中 key使用 session的id
        redisOperator.set("ORDER_TOKEN" + session.getId(), token, 600);
        return IMOOCJSONResult.ok(token);
    }


    @ApiOperation(value = "用户下单", notes = "用户下单", httpMethod = "POST")
    @PostMapping("/create")
    public IMOOCJSONResult create(@RequestBody SubmitOrderBO submitOrderBO, HttpServletRequest request, HttpServletResponse response){

        // 获取用户传过来的token值
        String orderTokenKey = "ORDER_TOKEN_" + request.getSession().getId();
        /**
         还存在一种情况，当一个用户请求并发的情况下，两个请求同时获取到了token，并且token在redis中
         存在并且正确，那就又可以创建订单了，没保证幂等性，并发的时候还是不可以，所以还要使用分布式锁
         */
        String lockKey = "LOCK_KEY_" + request.getSession().getId();
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(5, TimeUnit.SECONDS);

        try {
            // 到redis中去获取之前存在里面的token值
            String orderToken = redisOperator.get(orderTokenKey);
            // 没有获取到了 说明是不是同一个或者没有token
            if (StringUtils.isBlank(orderToken)) throw new RuntimeException("orderToken不存在");
            boolean correctToken = orderToken.equals(submitOrderBO.getToken());
            if (!correctToken) throw new RuntimeException("orderToken不正确");
            /**
             提交订单完成后，删除redis中的token，重复提交订单，直接抛出异常了---到这里就保证了 幂等性了
             */
            redisOperator.del(orderTokenKey);
        }catch (Exception e) {
           e.printStackTrace();
        }finally {
           lock.unlock();
        }


        if (submitOrderBO.getPayMethod() != PayMethod.WEIXIN.type
           && submitOrderBO.getPayMethod()!= PayMethod.ALIPAY.type) {
            return IMOOCJSONResult.errorMsg("支付方式不支持");
        }

//        System.out.println(submitOrderBO.toString());
        String shopcartJson= redisOperator.get(FOODIE_SHOPCART + ":" + submitOrderBO.getUserId());
        List<ShopcartBO> shopcartList = null;
        if (StringUtils.isBlank(shopcartJson)) {
            return IMOOCJSONResult.errorMsg("购物车数据不争取无阿");
        }
        /** redis中已经有购物车了 **/
        JsonUtils.jsonToList(shopcartJson, ShopcartBO.class);



        // 1: 创订订单
        PlaceOrderBO orderBO = new PlaceOrderBO(submitOrderBO, shopcartList);
        OrderVO orderVO = ordersService.createOrder(orderBO);
        String orderId = orderVO.getOrderId();

        // 2：创建订单以后，移除购物车中已经结算（已提交）的的商品
        /**
         * 1001,
         * 1002, --> 用户购买
         * 1003, --> 用户购买
         * 1004
         */
        shopcartList.removeAll(orderVO.getGetToBeRemovedShopcatList());
        // 根据用户id去清理
        redisOperator.set(FOODIE_SHOPCART + ":" + submitOrderBO.getUserId(), JsonUtils.objectToJson(shopcartList));
        // TODO 整合redis之后，晚上购物车中的已结算商品，并且需要同步到前端的cookie
        CookieUtils.setCookie(request,response,FOODIE_SHOPCART, JsonUtils.objectToJson(shopcartList),true);


        // 3：向支付中新发送当前订单，用于保存支付中心的订单数据
        MerchantOrdersVO merchantOrdersVO = new MerchantOrdersVO();
        merchantOrdersVO.setReturnUrl(payReturnUrl);
        /**
             怎么发送构建好的商户商户订单传递到支付中去，如何调用另一个系统呢？
             可以使用 http 请求， 也可以使用 Spring  的 restTemplate 这种rest风格的请求
             把 MerchantOrderVO 传递过去， 对方接受， 处理即可。 以 json 的形式进行处理
         */

        // 为了方便测试购买，所以购买的支付金额都同意改成1分钱
        merchantOrdersVO.setAmount(1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        /***
         * 要连接到支付中心（中心给平台配置权限）， 需要账号密码，才可以
         */
        headers.add("immocUserID","imooc");
        headers.add("password","imooc");

        /** 发起请求, 如何构建这个 postForEntity 对象呢？  */
        HttpEntity<MerchantOrdersVO> entity = new HttpEntity<>(
                                                            merchantOrdersVO,
                                                            headers);
        /**
         * paymentUrl : 要调用的地址
         * entity ：传输过去的 Eentity -> 包装了 我们要传过去的订单对象信息
         * IMOOCJSONResult.class ：返回被 ResponseEntity 包装过的 class 类型
         */
        ResponseEntity<IMOOCJSONResult> responseEntity =
                restTemplate.postForEntity(paymentUrl,
                                            entity,
                                            IMOOCJSONResult.class);
        IMOOCJSONResult paymentResult = responseEntity.getBody();

        if (paymentResult.getStatus() != 200) {
            return IMOOCJSONResult.errorMsg("支付中心订单创建失败，请联系管理员");
        }

        return IMOOCJSONResult.ok(orderId);
    }

    @PostMapping("/notifyMerchantOrderPaid")
    public Integer notifyMerchantOrderPaid(String merchantOrderId){
        ordersService.updateOrderStatus(merchantOrderId, OrderStatusEnum.WAIT_DELIVER.type);
        return HttpStatus.OK.value();
    }

    @PostMapping("/getPaidOrderInfo")
    public IMOOCJSONResult getPaidOrderInfo(String orderId){
        OrderStatus orderStatus = ordersService.queryOrderStatusInfo(orderId);
        return IMOOCJSONResult.ok(orderStatus);
    }

}
