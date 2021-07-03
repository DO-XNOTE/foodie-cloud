package com.imooc.user.service;


import com.imooc.user.pojo.UserAddress;
import com.imooc.user.pojo.bo.AddressBO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @描述:
 * @Author
 * @Since 2021/3/30 14:09
 */
@RequestMapping("address-api")
public interface AddressService {

    /**
     * 根据用户的 id 查询用户的收获列表
     * @param userId
     * @return
     */
    @GetMapping("addressList")
    public List<UserAddress> queryAll(@RequestParam("userId") String userId);

    /***
     * 用户新增地址
     * @param addressBO
     * @return
     */
    @PostMapping("address")
    public void addNewUserAddressList(@RequestBody AddressBO addressBO);

    /**
     * 用户性爱地址
     * @param addressBO
     */
    @PutMapping("address")
    public void updateUsersAddress(@RequestParam AddressBO addressBO);

    /**
     * 根据用户id、和地址id删除对应的用户地址 id
     * @param userId
     * @param addressId
     */
    @DeleteMapping("address")
    public void deleteUserAddress(@RequestParam("userId")String userId,
                                  @RequestParam("addressId")String addressId);

    /**
     * 修改默认地址
     * @param userId
     * @param addressId
     */
    @PostMapping("setDefaultAddress")
    public void updateUserAddressToBeDefault(@RequestParam("userId")String userId,
                                             @RequestParam("addressId")String addressId);

    /**
     * 根据用户id和地址id，嘻哈寻具体的用户地址对象信息
     * @param userId
     * @param addressId
     * @return
     */
    @GetMapping("queryAddress")
    public UserAddress queryUserAddress(@RequestParam("userId")String userId,
                                       @RequestParam(value = "addressId", required = false) String addressId);
}
