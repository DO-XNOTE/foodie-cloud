package com.imooc.order.service.impl.fallback.itemservice;

import com.google.common.collect.Lists;
import com.imooc.item.pojo.vo.MyCommentVO;
import com.imooc.pojo.PagedGridResult;
import feign.hystrix.FallbackFactory;

import java.util.Map;

/**
 * 工厂模式，  其实好多设计模式都是浑水摸鱼，大部分都是围绕着多态（接口啊之类的）继承 来变形出来的
 */
@SuppressWarnings("all")
public class ItemCommentFallbackFactory implements FallbackFactory<ItemCommentsFeignClient> {
    @Override
    public ItemCommentsFeignClient create(Throwable cause) {
        return new ItemCommentsFeignClient() {
            @Override
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
        };
    }
}
