package com.imooc.order.pojo.bo.center;

import com.imooc.pojo.ShopcartBO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @描述:
 * @Author
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderBO {
    private SubmitOrderBO order;
    private List<ShopcartBO> items;


}
