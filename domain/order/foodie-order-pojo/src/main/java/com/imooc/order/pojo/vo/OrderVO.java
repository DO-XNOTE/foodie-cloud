package com.imooc.order.pojo.vo;


import com.imooc.pojo.ShopcartBO;

import java.util.List;

/**
 * @描述:
 * @Author
 */
public class OrderVO {
    private String orderId;
    private MerchantOrdersVO merchantOrdersVO;

    private List<ShopcartBO> getToBeRemovedShopcatList;

    public List<ShopcartBO> getGetToBeRemovedShopcatList() {
        return getToBeRemovedShopcatList;
    }

    public void setGetToBeRemovedShopcatList(List<ShopcartBO> getToBeRemovedShopcatList) {
        this.getToBeRemovedShopcatList = getToBeRemovedShopcatList;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public MerchantOrdersVO getMerchantOrdersVO() {
        return merchantOrdersVO;
    }

    public void setMerchantOrdersVO(MerchantOrdersVO merchantOrdersVO) {
        this.merchantOrdersVO = merchantOrdersVO;
    }
}
