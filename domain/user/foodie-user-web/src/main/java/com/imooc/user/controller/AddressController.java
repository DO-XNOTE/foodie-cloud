package com.imooc.user.controller;


import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.user.pojo.UserAddress;
import com.imooc.user.pojo.bo.AddressBO;
import com.imooc.user.service.AddressService;
import com.imooc.utils.MobileEmailUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @描述:
 * @Author
 * @Since 2021/3/27 18:05
 */
@Api(value = "地址相关", tags = "地址相关的api接口")
@RestController
@RequestMapping("address")
public class AddressController {

    /**
     * 用户在订单页面，可以针对收货地址做如下操作
     * 1: 查询用户的所有收货地址列表
     * 2: 新增收货地址
     * 3: 删除收货地址
     * 4：修改收货地址
     * 5：设置默认地址
     */
    @Autowired
    private AddressService addressService;

    @ApiOperation(value = "根据用户id查询用户的收货地址", notes = "根据用户id查询用户的收货地址", httpMethod = "POST")
    @PostMapping("/list")
    public IMOOCJSONResult sixNewItems(
            @ApiParam(name = "userId", value = "用户id",required = true)
            @RequestParam String userId){

        if (StringUtils.isBlank(userId)) {
            return IMOOCJSONResult.errorMsg("");
        }
        List<UserAddress> list = addressService.queryAll(userId);
        return IMOOCJSONResult.ok(list);
    }

    @ApiOperation(value = "用户新增地址", notes = "用户新增地址", httpMethod = "POST")
    @PostMapping("/add")    /** AddressBO 的JSON对象 做参数 */
    public IMOOCJSONResult add( @RequestBody AddressBO addressBO){

        IMOOCJSONResult checkRes = checkAddress(addressBO);
        if (checkRes.getStatus() != 200) {
            return checkRes;
        }
        addressService.addNewUserAddressList(addressBO);

        return IMOOCJSONResult.ok();
    }

    private IMOOCJSONResult checkAddress(AddressBO addressBO) {
        String receiver = addressBO.getReceiver();
        if (StringUtils.isBlank(receiver)) {
            return IMOOCJSONResult.errorMsg("收货人不能为空");
        }
        if (receiver.length() > 12) {
            return IMOOCJSONResult.errorMsg("收货人姓名不能太长");
        }

        String mobile = addressBO.getMobile();
        if (StringUtils.isBlank(mobile)) {
            return IMOOCJSONResult.errorMsg("收货人手机号不能为空");
        }
        if (mobile.length() != 11) {
            return IMOOCJSONResult.errorMsg("收货人手机号长度不正确");
        }
        boolean isMobileOk = MobileEmailUtils.checkMobileIsOk(mobile);
        if (!isMobileOk) {
            return IMOOCJSONResult.errorMsg("收货人手机号格式不正确");
        }

        String province = addressBO.getProvince();
        String city = addressBO.getCity();
        String district = addressBO.getDistrict();
        String detail = addressBO.getDetail();
        if (StringUtils.isBlank(province) ||
                StringUtils.isBlank(city) ||
                StringUtils.isBlank(district) ||
                StringUtils.isBlank(detail)) {
            return IMOOCJSONResult.errorMsg("收货地址信息不能为空");
        }
        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "用户修改地址", notes = "用户修改地址", httpMethod = "POST")
    @PostMapping("/update")    /** AddressBO 的JSON对象 做参数 */
    public IMOOCJSONResult update( @RequestBody AddressBO addressBO){
        if (StringUtils.isBlank(addressBO.getAddressId())) {
            return IMOOCJSONResult.errorMsg("修改地址错误：AddressId不能为空");
        }

        IMOOCJSONResult checkRes = checkAddress(addressBO);
        if (checkRes.getStatus() != 200) {
            return checkRes;
        }
        addressService.updateUsersAddress(addressBO);

        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "用户删除地址", notes = "用户删除地址", httpMethod = "POST")
    @PostMapping("/delete")
    public IMOOCJSONResult delete( @RequestParam String  userId,
                                   @RequestParam String  addressId){
        /**
         * 这个判断为空可不可以不加啊？ 因为他们两为空不会删除数据库任何内容的！！！
         * 答案： 不可以，如果为空，虽然删除不了任何内容，但是我们的数据库被用户请求到了，
         *       加了判断为空，数据库不会被用户请求到，如果有大量请求为空的请求，在不加
         *       判断的情况下，数据库的性能可能会收到影响， 万一被别人攻击也不太好吧，
         *       所以还是判断一下
         */
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(addressId)) {
            return IMOOCJSONResult.errorMsg("");
        }
        addressService.deleteUserAddress(userId,addressId);
        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "用户设置默认地址", notes = "用户设置默认地址", httpMethod = "POST")
    @PostMapping("/setDefault")
    public IMOOCJSONResult setDefault( @RequestParam String  userId,
                                   @RequestParam String  addressId){
        /**
         * 这个判断为空可不可以不加啊？ 因为他们两为空不会删除数据库任何内容的！！！
         * 答案： 不可以，如果为空，虽然删除不了任何内容，但是我们的数据库被用户请求到了，
         *       加了判断为空，数据库不会被用户请求到，如果有大量请求为空的请求，在不加
         *       判断的情况下，数据库的性能可能会收到影响， 万一被别人攻击也不太好吧，
         *       所以还是判断一下
         */
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(addressId)) {
            return IMOOCJSONResult.errorMsg("");
        }
        addressService.updateUserAddressToBeDefault(userId,addressId);
        return IMOOCJSONResult.ok();
    }

}
