package com.imooc.order.service.center;



import com.imooc.order.pojo.OrderItems;
import com.imooc.order.pojo.bo.center.OrderItemsCommentBO;
import com.imooc.pojo.PagedGridResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @描述:
 * @Author
 */
@RequestMapping("order-comments-api")
public interface MyCommentsService {

    /**
     * 根据订单id查询关联的评论
      * @param orderId
     * @return
     */
    @GetMapping("orderItems")
    public List<OrderItems> queryPendingComment(@RequestParam("orderId") String orderId);

    /**
     * 保存用户的评论
     * @param orderId
     * @param commentList
     */
    @PostMapping("saveOrderComments")
    public void saveComments(@RequestParam("orderId")String orderId,
                             @RequestParam("userId")String userId,
                             @RequestBody List<OrderItemsCommentBO> commentList);
// TODO 移动到商品中心去了 Item-comments-service
//    /**
//     * 查询我的评价分页
//     * @param userId
//     * @param page
//     * @param pageSize
//     * @return
//     */
//    public PagedGridResult queryMyComment(String userId, Integer page, Integer pageSize);
}
