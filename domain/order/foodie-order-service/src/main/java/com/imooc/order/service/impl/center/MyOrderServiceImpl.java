package com.imooc.order.service.impl.center;

import com.github.pagehelper.PageHelper;
import com.imooc.enums.OrderStatusEnum;
import com.imooc.enums.YesOrNo;

import com.imooc.order.mapper.OrderStatusMapper;
import com.imooc.order.mapper.OrdersMapper;
import com.imooc.order.mapper.OrdersMapperCustom;
import com.imooc.order.pojo.OrderStatus;
import com.imooc.order.pojo.Orders;
import com.imooc.order.pojo.vo.MyOrdersVO;
import com.imooc.order.pojo.vo.OrderStatusCountsVO;
import com.imooc.order.service.center.MyOrderService;
import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.pojo.PagedGridResult;
import com.imooc.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import tk.mybatis.mapper.entity.Example;

import javax.persistence.criteria.Order;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @描述:
 * @Author
 */
@Service
public class MyOrderServiceImpl extends BaseService implements MyOrderService {

    @Autowired
    private OrdersMapperCustom ordersMapperCustom;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult queryMyOrders(String userId,
                                         Integer orderStatus,
                                         Integer page,
                                         Integer pageSize) {
        Map<String, Object>  map = new HashMap<>();
        map.put("userId", userId);
        if (orderStatus != null) {
            map.put("orderStatus", orderStatus);
        }
        PageHelper.startPage(page,pageSize);
        List<MyOrdersVO> list = ordersMapperCustom.queryMyOrders(map);
        return  setterPagedGrid(list, page);

    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateDeliverOrderStatus(String orderId) {
        OrderStatus updateOrder = new OrderStatus();
        updateOrder.setOrderStatus(OrderStatusEnum.WAIT_DELIVER.type);
        updateOrder.setDeliverTime(new Date());

        Example example = new Example(OrderStatus.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderId",orderId);
        criteria.andEqualTo("orderStatus",OrderStatusEnum.WAIT_DELIVER.type);

        orderStatusMapper.updateByExampleSelective(updateOrder, example);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Orders queryMyOrder(String orderId, String userId) {
        // 可以通过 Example 去查询， 也可以使用 对象去查询
        Orders orders = new Orders();
        orders.setUserId(userId);
        orders.setId(orderId);
        orders.setIsDelete(YesOrNo.NO.type);
        /** 如果这里查询不到一条记录，那说明被接口被恶意的调用了 ，所以Controller一定要做验证*/
        return  ordersMapper.selectOne(orders);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public boolean updateReceiveOrderStatus(String orderId) {
        OrderStatus updateOrder = new OrderStatus();
        updateOrder.setOrderStatus(OrderStatusEnum.SUCCESS.type);
        updateOrder.setSuccessTime(new Date());

        Example example = new Example(OrderStatus.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderId",orderId);
        criteria.andEqualTo("orderId",OrderStatusEnum.WAIT_RECEIVE.type);
        int result = orderStatusMapper.updateByExampleSelective(updateOrder, example);

        return result == 1 ? true : false;
    }

    /** 这里的删除订单要注意：是逻辑删除（其实就是更新擦操作）， 不是真的删除 */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public boolean deleteOrder(String userId, String orderId) {
        Orders updateOrder = new Orders();
        updateOrder.setIsDelete(YesOrNo.YES.type);
        updateOrder.setUpdatedTime(new Date());

        Example example = new Example(Orders.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id",orderId);
        criteria.andEqualTo("userId",userId);
        int result = ordersMapper.updateByExampleSelective(updateOrder, example);

        return result == 1 ? true : false;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public OrderStatusCountsVO getMyOrderStatusCounts(String userId) {
        Map<String, Object>  map = new HashMap<>();
        map.put("userId", userId);
        map.put("orderStatus",OrderStatusEnum.WAIT_PAY.type);
        int waitPayCounts = ordersMapperCustom.getMyOrderStatusCounts(map);

        map.put("orderStatus",OrderStatusEnum.WAIT_DELIVER.type);
        int waitDeliverCounts = ordersMapperCustom.getMyOrderStatusCounts(map);

        map.put("orderStatus",OrderStatusEnum.WAIT_RECEIVE.type);
        int waitReceiveCounts = ordersMapperCustom.getMyOrderStatusCounts(map);

        map.put("orderStatus",OrderStatusEnum.SUCCESS.type);
        map.put("isComment",YesOrNo.NO.type);
        int waitCommentCounts = ordersMapperCustom.getMyOrderStatusCounts(map);

        OrderStatusCountsVO countsVO =
                new OrderStatusCountsVO(waitPayCounts,
                                        waitDeliverCounts,
                                        waitReceiveCounts,
                                        waitCommentCounts);

        return countsVO;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult getOrdersTrend(String userId, Integer page, Integer pageSize) {
        Map<String, Object>  map = new HashMap<>();
        map.put("userId", userId);

        PageHelper.startPage(page, pageSize);
        List<OrderStatus> list = ordersMapperCustom.getMyOrderTrend(map);

        PageHelper.startPage(page, pageSize);

        return setterPagedGrid(list, page);
    }

    @Override
    public IMOOCJSONResult checkUserOrder(@RequestParam("userId") String userId,
                                          @RequestParam("orderId")String orderId) {
        Orders order = queryMyOrder(userId, orderId);
        if (order == null){
            return IMOOCJSONResult.errorMsg("订单不存在！！！");
        }
        return IMOOCJSONResult.ok(order);
    }
}
