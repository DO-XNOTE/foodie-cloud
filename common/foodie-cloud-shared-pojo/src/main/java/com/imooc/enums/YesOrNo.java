package com.imooc.enums;

/**
 * @描述:  是否，枚举
 * @Author
 * @Since 2021/3/29 17:29
 */
public enum YesOrNo {
    NO(0,"否"),
    YES(1,"是");

    public final Integer type;
    public final String value;

    YesOrNo(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
