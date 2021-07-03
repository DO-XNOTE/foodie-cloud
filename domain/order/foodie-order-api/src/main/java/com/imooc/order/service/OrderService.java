package com.imooc.order.service;


import com.imooc.order.pojo.OrderStatus;
import com.imooc.order.pojo.bo.center.PlaceOrderBO;
import com.imooc.order.pojo.vo.OrderVO;
import org.springframework.web.bind.annotation.*;

/**
 * @描述:
 * @Author
 * @Since 2021/3/30 14:09
 */
@RequestMapping("order-api")
public interface OrderService {

    /**
     * 用于创建订单相关信息
     * @param submitOrderBO
     */
    @PostMapping("placeOrder")
    public OrderVO createOrder(@RequestBody PlaceOrderBO orderBO);

    /**
     * 修改订单状态
     * @param orderId
     * @param orderStatus
     */
    @PostMapping("updateStatus")
    public void updateOrderStatus(@RequestParam("orderId") String orderId,
                                  @RequestParam("orderStatus") Integer orderStatus);

    /**
     * 查看订单状态
     * @param orderId
     * @return
     */
    @GetMapping("orderStatus")
    public OrderStatus queryOrderStatusInfo(@RequestParam("orderId")String orderId);

    /**
     * 关闭超时未支付订单
     */
    @PostMapping("closePendingOrders")
    public void closeOrder();

}
