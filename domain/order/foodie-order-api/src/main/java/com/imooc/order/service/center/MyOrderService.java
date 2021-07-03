package com.imooc.order.service.center;


import com.imooc.order.pojo.Orders;
import com.imooc.order.pojo.vo.OrderStatusCountsVO;
import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.pojo.PagedGridResult;
import org.springframework.web.bind.annotation.*;

/**
 * @描述:
 * @Author
 */
@RequestMapping("myOrder-api")
public interface MyOrderService {

    /**
     * 查询我的订单
     * @param userId
     * @param orderStatus
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("order/query")
    public PagedGridResult queryMyOrders(@RequestParam("userId")String userId,
                                         @RequestParam("orderStatus")Integer orderStatus,
                                         @RequestParam("page")Integer page,
                                         @RequestParam("pageSize")Integer pageSize);


    /**
     * 订单状态改成商家发货 ————> 商家发货
     * @param orderId
     */
    @PostMapping("order/deliver")
    public void updateDeliverOrderStatus(@RequestParam("orderId")String orderId);

    /**
     * 查询订单
     * @param orderId
     * @param userId
     * @return
     */
    @GetMapping("order/details")
    public Orders queryMyOrder(@RequestParam("orderId")String orderId,
                               @RequestParam("userId")String userId);

    /**
     *  更新订单状态 ————> 确认收货
     * @param orderId
     * @return
     */
    @PostMapping("order/recevied")
    public boolean updateReceiveOrderStatus(@RequestParam("orderId")String orderId);

    /**
     *  更新订单状态 ————> 删除(这是一个逻辑删除， 不是真正的删除)
     * @param orderId
     * @return
     */
    @DeleteMapping("order")
    public boolean deleteOrder(@RequestParam("userId")String userId,
                               @RequestParam("orderId")String orderId);

    /**
     * 查询我的订单
     * @param userId
     * @return
     */
    @GetMapping("order/counts")
    public OrderStatusCountsVO getMyOrderStatusCounts(@RequestParam("userId")String userId);

    /**
     * 获得分页的订单动向
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("order/trend")
    public PagedGridResult getOrdersTrend(@RequestParam("userId")String userId,
                                          @RequestParam("page")Integer page,
                                          @RequestParam("pageSize")Integer pageSize);



    /**
     *  用于验证用户和订单是否由关联关系， 避免非法用户调用
     * @return
     */
    @GetMapping("checkUserOrder")
    public IMOOCJSONResult checkUserOrder(@RequestParam("userId")String userId,
                                          @RequestParam("orderId")String orderId);


}
