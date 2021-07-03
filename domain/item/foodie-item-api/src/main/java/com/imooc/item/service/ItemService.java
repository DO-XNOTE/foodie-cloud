package com.imooc.item.service;

import com.imooc.item.pojo.*;
import com.imooc.item.pojo.ItemsImg;
import com.imooc.item.pojo.ItemsParam;

import com.imooc.item.pojo.vo.CommentLevelCountsVO;
import com.imooc.item.pojo.vo.ShopcartVO;
import com.imooc.pojo.PagedGridResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 *         好处：1：
 * @描述:   因为我们的接口层是要被外部微服务调用的，必然是要有请求的，之后会申明Controller，还有一些路径的配置
 * @Author 这样在接口层的注解可以继承到他自己具体的实现类中
 *             2：
 *         使用 Feign 服务间调用的时候，我一个作为下游的服务调用 ItemService 服务，Feign调用，如果接口上已经声明了@RequestMapping
 *         以及 GetMapping的路径信息， 调用方拿到接口就可以直接去寻址了，  这样下游运用避免了还需要单独配置一套寻址路径
 */
@RequestMapping("/item-api")
public interface ItemService {
    /**
     *  根据商品ID查询详情
     * @param itemId: 使用spring-cloud-feign进行调用的话，没有指定@RequestParam的话，会报错的
     * @return        默认情况，不使用feign调用，请求参数直接使用名称就可以
     */
    @GetMapping("item")
    public Items queryItemById(@RequestParam("itemId") String itemId);

    /**
     * 根据商品ID查询图片列表
     * @param itemId
     * @return
     */
    @GetMapping("itemImages")
    public List<ItemsImg> queryItemImgList(@RequestParam("itemId")String itemId);

    /**
     * 根据商品ID查询商品规格
     * @param itemId
     * @return
     */
    @GetMapping("itemSpecs")
    public List<ItemsSpec> queryItemSpecList(@RequestParam("itemId")String itemId);

    /**
     * 根据商品ID查询商品参数（属性）
     * @param itemId
     * @return
     */
    @GetMapping("itemParam")
    public ItemsParam queryItemParam(@RequestParam("itemId")String itemId);

    /**
     * 根绝id查询商品的评价等级数量
     * @param itemId
     */
    @GetMapping("countComments")
    public CommentLevelCountsVO queryCommentsCounts(@RequestParam("itemId")String itemId);

    /**
     * 根据商品ID查询商品的评价(分页)
     * @param itemId
     * @param level
     * @return
     */
    @GetMapping("pageComments")
    public PagedGridResult queryPagedComments(@RequestParam("itemId")String itemId,
                                              @RequestParam(value = "level", required = false )Integer level,
                                              @RequestParam(value = "page", required = false )Integer page,
                                              @RequestParam(value = "pageSize", required = false )Integer pageSize);

//    /**
//     * 搜索商品列表
//     * @param keyWords
//     * @param sort
//     * @param page
//     * @param pageSize
//     * @return
//     */
//    public PagedGridResult searchItems(String keyWords,
//                                          String sort,
//                                          Integer page,
//                                          Integer pageSize);
//
//    /**
//     * 根据分类ID搜索商品列表
//     * @param catId
//     * @param sort
//     * @param page
//     * @param pageSize
//     * @return
//     */
//    public PagedGridResult searchItems(Integer catId,
//                                       String sort,
//                                       Integer page,
//                                       Integer pageSize);


    /**
     * 根据规格 ids 查询 最新的购物车中的商品数据（用于刷新渲染购物陈中商品数据）
     * @param specIds
     * @return
     */
    @GetMapping("getCartBySpecIds")
    public List<ShopcartVO> queryItemsSpecIds(@RequestParam("specIds")String specIds);

    /**
     * 根据商品规格的id获取规格对象的具体信息
     * @param specId
     * @return
     */
    @GetMapping("singleItemSpec")
    public ItemsSpec queryItemSpecById(@RequestParam("specId")String specId);

    /**
     * 根据商品获取商品的主图
     * @param itemId
     * @return
     */
    @GetMapping("primaryImage")
    public String queryItemMainImgById(@RequestParam("itemId")String itemId);

    /**
     * 减少库存
     * @param specId   这两个是必填的参数，就不写require了，默认就是true
     * @param buyCounts
     */
    @PostMapping("decreaseStock")
    public void  decreaseItemSpecStock(@RequestParam("specId")String specId,
                                       @RequestParam("buyCounts")int buyCounts);

}
