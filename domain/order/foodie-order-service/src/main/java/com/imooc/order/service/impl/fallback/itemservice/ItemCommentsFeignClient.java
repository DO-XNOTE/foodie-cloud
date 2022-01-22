package com.imooc.order.service.impl.fallback.itemservice;

import com.imooc.pojo.PagedGridResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(value = "foodie-item-service", fallback = ItemCommentFallbackFactory.class)
public interface ItemCommentsFeignClient {
    PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize);
    void saveComments(@RequestBody Map<String, Object> map);
}
