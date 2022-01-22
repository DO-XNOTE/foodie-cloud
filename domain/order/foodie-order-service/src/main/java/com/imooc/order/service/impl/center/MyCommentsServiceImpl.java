package com.imooc.order.service.impl.center;

import com.imooc.enums.YesOrNo;

import com.imooc.order.mapper.OrderItemsMapper;
import com.imooc.order.mapper.OrderStatusMapper;
import com.imooc.order.mapper.OrdersMapper;
import com.imooc.order.pojo.OrderItems;
import com.imooc.order.pojo.OrderStatus;
import com.imooc.order.pojo.Orders;
import com.imooc.order.pojo.bo.center.OrderItemsCommentBO;
import com.imooc.order.service.center.MyCommentsService;
import com.imooc.order.service.impl.fallback.itemservice.ItemCommentFallback;
import com.imooc.service.BaseService;

import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @描述:
 * @Author
 */
@SuppressWarnings("all")
@Service
public class MyCommentsServiceImpl extends BaseService implements MyCommentsService {

    @Autowired
    private OrderItemsMapper orderItemsMapper;

//    @Autowired
//    private ItemsCommentsMapperCustom itemsCommentsMapperCustom;
    /** 盐城方法调用，需要 LoadBalancerClient 和 RestTemplate */
    // TODO 暂时这样，后面使用feign调用
    @Autowired
    private LoadBalancerClient client;
    @Autowired
    private RestTemplate restTemplate;


    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
//    private ItemCommentsService itemCommentsService;   使用服务降级的服务
    private ItemCommentFallback itemCommentFallback;


    @Autowired
    private Sid sid;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<OrderItems> queryPendingComment(String orderId) {
        OrderItems query = new OrderItems();
        query.setOrderId(orderId);
        return orderItemsMapper.select(query);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public void saveComments(String orderId,String userId,
                             List<OrderItemsCommentBO> commentList) {
        // 1：保存评价 items_comments 表
        for (OrderItemsCommentBO oic : commentList) {
            oic.setCommentId(sid.nextShort());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("commentList", commentList);
//        itemsCommentsMapperCustom.saveComments(map);
        /**  LoadBanlance  */
        ServiceInstance instance = client.choose("FOODIE-USER-SERVICE");
        String url = String.format("http://%s:%s/item-comments-api/saveComments");
        instance.getHost();
        instance.getPort();
        /**  RestTemplate 发起调用 */
        restTemplate.postForLocation(url, map);

        // 2：修改订单表已评价 orders 表
        Orders order = new Orders();
        order.setId(orderId);
        order.setIsComment(YesOrNo.YES.type);
        ordersMapper.updateByPrimaryKeySelective(order);

        // 3：修改订单状态表留言时间 order_status 表
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCommentTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(orderStatus);

    }
//    TODO 移动到itemcommentsService
//    @Transactional(propagation = Propagation.SUPPORTS)
//    @Override
//    public PagedGridResult queryMyComment(String userId,
//                                          Integer page,
//                                          Integer pageSize) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("userId",userId);
//        PageHelper.startPage(page, pageSize);
//
//        List<MyCommentVO> list = itemsCommentsMapperCustom.queryMyComments(map);
//        return setterPagedGrid(list, page);
//    }
}
