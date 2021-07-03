package com.imooc.order.controller.center;

import com.imooc.controller.BaseController;

import com.imooc.order.pojo.vo.OrderStatusCountsVO;
import com.imooc.order.service.center.MyOrderService;
import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.pojo.PagedGridResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @描述:
 * @Author
 */
@Api(value = "用户中心订单",tags = "用户中心我的订单相关接口")
@RestController
@RequestMapping("myorders")
public class MyOrdersController extends BaseController {

    @Autowired
    private MyOrderService myOrderService;

    @ApiOperation(value = "获得订单概况数概况", notes = "获得订单概况数概况", httpMethod = "POST")
    @PostMapping("/statusCounts")
    public IMOOCJSONResult statusCounts(
            @ApiParam(name = "userId", value = "用户id" , required = true)
            @RequestParam String userId){

        if (StringUtils.isBlank(userId)) {
            return IMOOCJSONResult.errorMsg(null);
        }
        OrderStatusCountsVO result = myOrderService.getMyOrderStatusCounts(userId);
        return IMOOCJSONResult.ok(result);
    }

    @ApiOperation(value = "查询订单列表", notes = "查询订单列表", httpMethod = "POST")
    @PostMapping("/query")
    public IMOOCJSONResult query(
            @ApiParam(name = "userId", value = "用户id" , required = true)
            @RequestParam String userId,
            @ApiParam(name = "orderStatus", value = "订单状态" , required = false)
            @RequestParam Integer orderStatus,
            @ApiParam(name = "page", value = "查询下一页的第几页" , required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize", value = "分页的每一页显示的参数" , required = false)
            @RequestParam Integer pageSize){

        if (StringUtils.isBlank(userId)) {
            return IMOOCJSONResult.errorMsg(null);
        }
        if (page == null){
            page = 1;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult grid = myOrderService.queryMyOrders(
                                                        userId,
                                                        orderStatus,
                                                        page,
                                                        pageSize);
        return IMOOCJSONResult.ok(grid);
    }
    /** 商家发货没有后端， 所以这个接口仅仅是用于模拟发货 */
    @ApiOperation(value = "查询订单列表", notes = "查询订单列表", httpMethod = "GET")
    @GetMapping("/deliver")
    public IMOOCJSONResult deliver(
            @ApiParam(name = "orderId", value = "订单id" , required = true)
            @RequestParam String orderId){

        if (StringUtils.isBlank(orderId)) {
            return IMOOCJSONResult.errorMsg("订单id不能为空");
        }
        myOrderService.updateDeliverOrderStatus(orderId);
        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "用户确认收货", notes = "用户确认收货", httpMethod = "POST")
    @PostMapping("/confirmReceive")
    public IMOOCJSONResult confirmReceive(
            @ApiParam(name = "orderId", value = "订单id" , required = true)
            @RequestParam String orderId,
            @ApiParam(name = "userId", value = "用户id" , required = true)
            @RequestParam String userId){

        /** 如果这里查询不到一条记录，那说明被接口被恶意的调用了 ，
         * 所以Controller一定要做验证 ， 除了订单查询，订单删除也需要验证，所以写一个通用的验证 */

        IMOOCJSONResult checkResult = myOrderService.checkUserOrder(userId, orderId);

        if (checkResult.getStatus() != HttpStatus.OK.value()) {
            return checkResult;
        }

        boolean res = myOrderService.updateReceiveOrderStatus(orderId);
        if (!res) {
            return IMOOCJSONResult.errorMsg("订单确认收货失败");
        }
        return IMOOCJSONResult.ok();
    }

    /**
     上面的查询订单，和下面的删除订单，很多代码是一样的，按道理可以合成一个接口调用，
     但是这里不建议这样做，代码的耦合度增加了，后续如果有其他的业务操作到了，就到导致不容易扩展
     接口也是这样的道理
     */
    @ApiOperation(value = "用户删除订单", notes = "用户删除订单", httpMethod = "POST")
    @PostMapping("/delete")
    public IMOOCJSONResult delete(
            @ApiParam(name = "orderId", value = "订单id" , required = true)
            @RequestParam String orderId,
            @ApiParam(name = "userId", value = "用户id" , required = true)
            @RequestParam String userId){

        /** 如果这里查询不到一条记录，那说明被接口被恶意的调用了 ，
         * 所以Controller一定要做验证 ， 除了订单查询，订单删除也需要验证，所以写一个通用的验证 */

        IMOOCJSONResult checkResult = myOrderService.checkUserOrder(userId, orderId);

        if (checkResult.getStatus() != HttpStatus.OK.value()) {
            return checkResult;
        }
        boolean res = myOrderService.deleteOrder(userId, orderId);
        if (!res) {
            return IMOOCJSONResult.errorMsg("订单删除失败");
        }
        myOrderService.updateDeliverOrderStatus(orderId);
        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "查询订单动向", notes = "查询订单动向", httpMethod = "POST")
    @PostMapping("/trend")
    public IMOOCJSONResult trend(
            @ApiParam(name = "userId", value = "用户id" , required = true)
            @RequestParam String userId,
            @ApiParam(name = "page", value = "查询下一页的第几页" , required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize", value = "分页的每一页显示的参数" , required = false)
            @RequestParam Integer pageSize) {

        if (StringUtils.isBlank(userId)) {
            return IMOOCJSONResult.errorMsg(null);
        }
        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult grid = myOrderService.getOrdersTrend(
                userId,
                page,
                pageSize);
        return IMOOCJSONResult.ok(grid);
    }
}
