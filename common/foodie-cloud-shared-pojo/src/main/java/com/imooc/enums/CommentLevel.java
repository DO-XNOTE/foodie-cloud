package com.imooc.enums;

/**
 * @描述:  商品等级评价，枚举
 * @Author
 * @Since 2021/3/29 17:29
 */
public enum CommentLevel {
    GOOD(1,"好评"),
    NORMAL(2,"中评"),
    BAD(3,"差评");

    public final Integer type;
    public final String value;

    CommentLevel(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
