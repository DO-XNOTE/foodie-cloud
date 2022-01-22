package com.imooc.order.service.impl.fallback.itemservice;

import com.google.common.collect.Lists;
import com.imooc.item.pojo.vo.MyCommentVO;
import com.imooc.item.service.ItemCommentsService;
import com.imooc.pojo.PagedGridResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * （2）原始的Feign接口不定义RequestMapping注解
 *  优点：启动的时候直接扫描包即可，不用指定加载接口
 *  缺点：a，服务提供者要额外的配置路径访问放入注解
 *        b，任何情况下，即使不需要调用端定义fallback类，服务调用者都
 */
@SuppressWarnings("all")
@Component
@RequestMapping("Jokejoke")
public class ItemCommentFallback implements ItemCommentsFeignClient {

    @Override
    // HystrixCommand - 可以实现多级降级
    public PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize) {
        MyCommentVO commentsVo = new MyCommentVO();
        commentsVo.setContent("正在加载中....");

        PagedGridResult result = new PagedGridResult();
        result.setRows(Lists.newArrayList(commentsVo));
        result.setTotal(1);
        result.setRecords(1);
        return result;
    }

    @Override
    public void saveComments(Map<String, Object> map) {

    }
}
