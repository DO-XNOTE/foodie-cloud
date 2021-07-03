package com.imooc.order.service.impl;

import com.imooc.enums.OrderStatusEnum;
import com.imooc.enums.YesOrNo;
import com.imooc.item.pojo.Items;
import com.imooc.item.pojo.ItemsSpec;
import com.imooc.order.mapper.OrderItemsMapper;
import com.imooc.order.mapper.OrderStatusMapper;
import com.imooc.order.mapper.OrdersMapper;
import com.imooc.order.pojo.OrderItems;
import com.imooc.order.pojo.OrderStatus;
import com.imooc.order.pojo.Orders;
import com.imooc.order.pojo.bo.center.PlaceOrderBO;
import com.imooc.order.pojo.bo.center.SubmitOrderBO;
import com.imooc.order.pojo.vo.MerchantOrdersVO;
import com.imooc.order.pojo.vo.OrderVO;
import com.imooc.order.service.OrderService;
import com.imooc.pojo.ShopcartBO;

import com.imooc.user.pojo.UserAddress;
import com.imooc.utils.DateUtil;
import com.imooc.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @描述:
 * @Author
 * @Since 2021/3/30 14:11
 */
@RestController
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderItemsMapper orderItemsMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private Sid sid;
//  TODO 学了 feign 之后打开
//    @Autowired
//    private AddressService addressService;
//
//    @Autowired
//    private ItemService itemService;

    // TODO 暂时这样，后面使用feign调用
    @Autowired
    private LoadBalancerClient client;
    @Autowired
    private RestTemplate restTemplate;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public OrderVO createOrder(PlaceOrderBO orderBO) {
        List<ShopcartBO> shopcartList = orderBO.getItems();
        SubmitOrderBO submitOrderBO = orderBO.getOrder();
        String userId = submitOrderBO.getUserId();
        String addressId = submitOrderBO.getAddressId();
        String itemSpecIds = submitOrderBO.getItemSpecIds();
        Integer payMethod = submitOrderBO.getPayMethod();
        String leftMsg = submitOrderBO.getLeftMsg();
        // 包邮费用设置为0
        Integer postAmount = 0;

        /** 满足分布式 id 的需求，全局唯一的，会根据不同机器生成不同的id：看视屏讲解*/
        String orderId = sid.nextShort();

        // FIXME 等待feign进行简化
//        UserAddress address = addressService.queryUserAddress(userId, addressId);
        ServiceInstance instance = client.choose("FOODIE-USER-SERVICE");
        String url = String.format("http://%s:%s/address/queryAddress" +
                "userId=%s&addressId=%s",
                instance.getHost(),
                instance.getPort(),
                userId , addressId);
        /**  RestTemplate 发起调用 */
        UserAddress address = restTemplate.getForObject(url, UserAddress.class);

        // 1:新订单数据保存
        Orders newOrder = new Orders();
        newOrder.setId(orderId);
        newOrder.setUserId(userId);

        newOrder.setReceiverName(address.getReceiver());
        newOrder.setReceiverMobile(address.getMobile());
        newOrder.setReceiverAddress(address.getProvince() + " "
                + address.getCity() + " " + address.getDetail());

        newOrder.setPostAmount(postAmount);

        newOrder.setPayMethod(payMethod);
        newOrder.setLeftMsg(leftMsg);
        newOrder.setIsComment(YesOrNo.NO.type);
        newOrder.setIsDelete(YesOrNo.NO.type);
        newOrder.setCreatedTime(new Date());
        newOrder.setUpdatedTime(new Date());

        // 2：循环根据itemSapecIds保存订单商品信息表
        String itemSpecIdArr[] = itemSpecIds.split(",");
        Integer totalAmount = 0; // 商品原价累计
        Integer realPayAomunt = 0; // 优惠的实际价格累计
        /** 清理已结算商品 ，放到controller中去做*/
        List<ShopcartBO> toBeRemoveShopCartList = new ArrayList<>();
        // TODO 整合redis之后，商品购买的数量重新从redis的购物车中获取
        for (String itemSpecId : itemSpecIdArr) {
            /** 整合redis后，商品购买的数量重新从redis购物车中获取 **/
            ShopcartBO cartItem = getBuyCountsFromShopcat(shopcartList, itemSpecId);
            int buyCounts = cartItem.getBuyCounts();
            toBeRemoveShopCartList.add(cartItem);

            // 2.1： 根据规格id，查询规格的具体信息，主要获取价格
            // FIXME 等待feign进行简化
//            ItemsSpec itemSpec = itemService.queryItemSpecById(itemSpecId);
            ServiceInstance itemInstance = client.choose("FOODIE-ITEM-SERVICE");
            String itemServiceUrl = String.format("http://%s:%s/item-api/singleItemSpec" +
                                                "?specId=%s",
                                                itemInstance.getHost(),
                                                itemInstance.getPort(),
                                                itemSpecId);
            /**  RestTemplate 发起调用 */
            ItemsSpec itemSpec = restTemplate.getForObject(itemServiceUrl, ItemsSpec.class);

            totalAmount += itemSpec.getPriceNormal() * buyCounts;
            realPayAomunt += itemSpec.getPriceDiscount() * buyCounts;

            // 2.2:根据商品id，获取商品信息以及商品图片
            String itemId = itemSpec.getItemId();
            // FIXME 等待feign进行简化
//            Items item = itemService.queryItemById(itemId);
            ServiceInstance itemByIdInstance = client.choose("FOODIE-ITEM-SERVICE");
            String itemByIdUrl = String.format("http://%s:%s/item-api/item?itemId=%s" +
                                            itemByIdInstance.getHost(),
                                            itemByIdInstance.getPort(),
                                            itemId);
            Items item = restTemplate.getForObject(itemByIdUrl, Items.class);
            // FIXME 等待feign进行简化
//            String imgUrl = itemService.queryItemMainImgById(itemId);
            ServiceInstance itemByImgInstance = client.choose("FOODIE-ITEM-SERVICE");
            String itemByImgUrl = String.format("http://%s:%s/item-api/primaryImage?itemId=%s" +
                                            itemByImgInstance.getHost(),
                                            itemByImgInstance.getPort(),
                                            itemId);
            String imgUrl = restTemplate.getForObject(itemByImgUrl, String.class);

            // 2.3:循环保存子订单数据到数据库
            String subOrderId = sid.nextShort();
            OrderItems subOrderItem = new OrderItems();
            subOrderItem.setId(subOrderId);
            subOrderItem.setOrderId(orderId);
            subOrderItem.setItemName(item.getItemName());
            subOrderItem.setItemImg(imgUrl);
            subOrderItem.setBuyCounts(buyCounts);
            subOrderItem.setItemSpecId(itemSpecId);
            subOrderItem.setItemName(itemSpec.getName());
            subOrderItem.setPrice(itemSpec.getPriceDiscount());
            orderItemsMapper.insert(subOrderItem);

            //2.4:再用户提交订单后，规格表中需要扣除库存
            // FIXME 等待feign进行简化
//            itemService.decreaseItemSpecStock(itemSpecId, buyCounts);
            String specId = itemSpecId;
            ServiceInstance stockInstance = client.choose("FOODIE-ITEM-SERVICE");
            String stockUrl = String.format("http://%s:%s/item-api/decreaseStock" +
                            "?specId=%s&buyCounts=%s",
                    stockInstance.getHost(),
                    stockInstance.getPort(),
                    specId, buyCounts);
            restTemplate.postForLocation(stockUrl, Void.class);
            /**
            分布式事务测试： 抛出异常之后，如果既没有口处库存，也没有生成订单，那么分布式事务就是有效的
            创建订单，扣除库存是在两台机器上的数据库上
             */
//            throw new RuntimeException("测试分布式事务");

        }

        newOrder.setTotalAmount(totalAmount);
        newOrder.setRealPayAmount(realPayAomunt);


        // 3: 订单保存状态
        OrderStatus waitPayOrderStatus = new OrderStatus();
        waitPayOrderStatus.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        waitPayOrderStatus.setCreatedTime(new Date());
        orderStatusMapper.insert(waitPayOrderStatus);


        /******************************微信之开始**************************************/
        /** 构建商户订单，用于传递给支付中心 */
        MerchantOrdersVO merchantOrdersVO = new MerchantOrdersVO();
        merchantOrdersVO.setMerchantOrderId(orderId);
        merchantOrdersVO.setMerchantUserId(userId);
        merchantOrdersVO.setAmount(realPayAomunt + postAmount);
        merchantOrdersVO.setPayMethod(payMethod);

        /** 构建自定义订单 VO */
        OrderVO orderVO = new OrderVO();
        orderVO.setOrderId(orderId);
        orderVO.setMerchantOrdersVO(merchantOrdersVO);
        orderVO.setGetToBeRemovedShopcatList(toBeRemoveShopCartList);
        return orderVO;
    }

    /**
     * 从redis购物车中获取商品， 目的：counts
     * @param shopcartList
     * @param specId
     * @return
     */
    private  ShopcartBO getBuyCountsFromShopcat(List<ShopcartBO> shopcartList, String specId){
        for (ShopcartBO cart: shopcartList) {
            if (cart.getSpecId().equals(specId)) {
                return cart;
            }
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateOrderStatus(String orderId, Integer orderStatus) {
        OrderStatus paidStatus = new OrderStatus();
        paidStatus.setOrderId(orderId);
        paidStatus.setOrderStatus(orderStatus);
        paidStatus.setPayTime(new Date());

        orderStatusMapper.updateByPrimaryKeySelective(paidStatus);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public OrderStatus queryOrderStatusInfo(String orderId) {
        OrderStatus status = orderStatusMapper.selectByPrimaryKey(orderId);
        String s = JsonUtils.objectToJson(status);
        List<OrderStatus> list = new ArrayList<>();
        list.add(status);
        int size = list.size();
        log.info("size = " + size + " ========== status {} = " + s);
        return status;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void closeOrder() {
        // 查询所有未支付订单，判断事件是否超时（1天），超时则关闭交易
        OrderStatus queryOrder = new OrderStatus();
        queryOrder.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        List<OrderStatus> list = orderStatusMapper.select(queryOrder);

        for (OrderStatus os : list) {
            // 获得订单创建时间
            Date createTime = os.getCreatedTime();
            // 和当前时间进行对比
            int days = DateUtil.daysBetween(createTime, new Date());
            if (days >= 1) {
                // 超过1天，关闭订单
                doCloseOrder(os.getOrderId());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    void doCloseOrder(String orderId){
        OrderStatus close = new OrderStatus();
        close.setOrderId(orderId);
        close.setOrderStatus(OrderStatusEnum.CLOSE.type);
        close.setCloseTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(close);
    }
}
