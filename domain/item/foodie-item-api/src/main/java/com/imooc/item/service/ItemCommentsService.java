package com.imooc.item.service;

import com.imooc.pojo.PagedGridResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @描述:
 * @Author
 */
@RequestMapping("item-comments-api")
public interface ItemCommentsService {
    /**
     * 查询我的评价分页
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("myComments")
    public PagedGridResult queryMyComment(@RequestParam("userId") String userId,
                                          @RequestParam(value = "page",required = false)Integer page,
                                          @RequestParam(value = "pageSize",required = false)Integer pageSize);

    /**
     * 订单中心会调用
     * @param map
     */
    @PostMapping("saveComments")
    public void saveComments(@RequestParam Map<String, Object> map);

}
