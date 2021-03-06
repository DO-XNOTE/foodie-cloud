package com.imooc.item.controller;

import com.google.common.collect.ArrayListMultimap;
import com.imooc.controller.BaseController;
import com.imooc.item.pojo.Items;
import com.imooc.item.pojo.ItemsImg;
import com.imooc.item.pojo.ItemsParam;
import com.imooc.item.pojo.ItemsSpec;
import com.imooc.item.pojo.vo.CommentLevelCountsVO;
import com.imooc.item.pojo.vo.ItemInfoVO;
import com.imooc.item.pojo.vo.ShopcartVO;
import com.imooc.item.service.ItemService;
import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.pojo.PagedGridResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @描述:
 * @Author
 * @Since 2021/3/27 18:05
 */
@Api(value = "商品接口",tags = "商品信息展示的相关接口")
@RestController
@RequestMapping("items")
public class ItemsController extends BaseController {

    @Autowired
    private ItemService itemService;

    @ApiOperation(value = "查询商品详情", notes = "查询商品详情", httpMethod = "GET")
    @GetMapping("/info/{itemId}")
    public IMOOCJSONResult info(
            @ApiParam(name = "itemId", value = "商品id" , required = true)
            @PathVariable String itemId ){
        if (StringUtils.isBlank(itemId)) {
            return IMOOCJSONResult.errorMsg(null);
        }
        Items item = itemService.queryItemById(itemId);
        List<ItemsImg> itemImgList = itemService.queryItemImgList(itemId);
        List<ItemsSpec> itemSpecsList = itemService.queryItemSpecList(itemId);
        ItemsParam itemParams = itemService.queryItemParam(itemId);

        /**
         * 这里要返回4个结果，但是我们工具类只能返回一个结果，怎么办？
         * 很明显又要和VO 打交道了,把结果包装起来
         */
        ItemInfoVO itemInfoVO = new ItemInfoVO();
        itemInfoVO.setItem(item);
        itemInfoVO.setItemImgList(itemImgList);
        itemInfoVO.setItemSpecList(itemSpecsList);
        itemInfoVO.setItemParams(itemParams);
        /**谷歌的guawa中multimap使用可以简化代码*/
       /** ArrayListMultimap<Object, Object> multimap = ArrayListMultimap.create();*/
//        ArrayListMultimap<Object, Object> multimap = ArrayListMultimap.create();

        return IMOOCJSONResult.ok(itemInfoVO);
    }

    @ApiOperation(value = "查询商品评价等级", notes = "查询商品评价等级", httpMethod = "GET")
    @GetMapping("/commentLevel")
    public IMOOCJSONResult commentLevel(
            @ApiParam(name = "itemId", value = "商品id" , required = true)
            @RequestParam String itemId ){
        if (StringUtils.isBlank(itemId)) {
            return IMOOCJSONResult.errorMsg(null);
        }
        CommentLevelCountsVO countsVO = itemService.queryCommentsCounts(itemId);
        return IMOOCJSONResult.ok(countsVO);
    }

    @ApiOperation(value = "查询商品评论", notes = "查询商品评论", httpMethod = "GET")
    @GetMapping("/comments")
    public IMOOCJSONResult comments(
            @ApiParam(name = "itemId", value = "商品id" , required = true)
            @RequestParam String itemId,
            @ApiParam(name = "level", value = "评价等级" , required = false)
            @RequestParam Integer level,
            @ApiParam(name = "page", value = "查询下一页的第几页" , required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize", value = "分页的每一页显示的参数" , required = false)
            @RequestParam Integer pageSize){

        if (StringUtils.isBlank(itemId)) {
            return IMOOCJSONResult.errorMsg(null);
        }
        if (page == null){
            page = 1;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
            /** 为了更加的同用化一点 继承一个 BaseCopntroller */
        }
        PagedGridResult grid = itemService.queryPagedComments(
                                        itemId,
                                        level,
                                        page,
                                        pageSize);
        return IMOOCJSONResult.ok(grid);
    }




    /** 用于用户长时间未登录网站，刷新购物车中的数据（主要是商品价格） 类似京东淘宝就是这样做的 */
    @ApiOperation(value = "根据商品规格ids查找最新的商品数据", notes = "根据商品规格ids查找最新的商品数据", httpMethod = "GET")
    @GetMapping("/refresh")
    public IMOOCJSONResult refresh(
            @ApiParam(name = "itemSepcIds", value = "拼接的规格ids" , required = true, example = "1001,1003,1003")
            @RequestParam String itemSepcIds){

        if (StringUtils.isBlank(itemSepcIds)) {
            /* 你给我传递的是空，那我返回的你也是空，这样也是可以的 */
            return IMOOCJSONResult.ok();
        }
        List<ShopcartVO> list = itemService.queryItemsSpecIds(itemSepcIds);
        return IMOOCJSONResult.ok(list);
    }
}
