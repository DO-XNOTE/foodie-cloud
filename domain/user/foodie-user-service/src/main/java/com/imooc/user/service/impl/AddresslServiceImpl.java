package com.imooc.user.service.impl;

import com.imooc.enums.YesOrNo;
import com.imooc.user.mapper.UserAddressMapper;
import com.imooc.user.pojo.UserAddress;
import com.imooc.user.pojo.bo.AddressBO;
import com.imooc.user.service.AddressService;
import com.imooc.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * @描述:
 * @Author
 * @Since 2021/3/30 14:11
 */
@RestController
@Slf4j
public class AddresslServiceImpl implements AddressService {

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired  /** 唯一主键 */
    private Sid sid;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<UserAddress> queryAll(String userId) {
        UserAddress ua = new UserAddress();
        ua.setUserId(userId);
        List<UserAddress> list = userAddressMapper.select(ua);
        UserAddress address = list.get(0);
        String s = JsonUtils.objectToJson(address);
        log.info("size = " +  list.size() + "======= address {} = " + s);
        return list;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void addNewUserAddressList(AddressBO addressBO) {
        // 1:判断当前用户是否存在地址，如果没有，则新增为“默认地址”
        Integer isDefault = 0;
        List<UserAddress> addressList = this.queryAll(addressBO.getUserId());
        if (addressList == null || addressList.isEmpty() || addressList.size() == 0) {
            isDefault = 1;
        }
        UserAddress newAddress = new UserAddress();
        String addressId = sid.nextShort();

        /**
         AddressBO 对象里面有属性和 UserAddress 一摸一样的， 所以可以使用原型模式
         Spring里面有 BeanUtils.copyProperties()

         opyProperties(Object source, Object target)

         */
        BeanUtils.copyProperties(addressBO, newAddress);

        newAddress.setId(addressId);
        newAddress.setIsDefault(isDefault);
        newAddress.setCreatedTime(new Date());
        newAddress.setUpdatedTime(new Date());

        userAddressMapper.insert(newAddress);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateUsersAddress(AddressBO addressBO) {
        String addressId = addressBO.getAddressId();

        UserAddress pendingAddress = new UserAddress();
        BeanUtils.copyProperties(addressBO, pendingAddress);
        /** addressBO, pendingAddress 的id 是不一样的 需要单独设置 */
        pendingAddress.setId(addressId);
        pendingAddress.setUpdatedTime(new Date());

        userAddressMapper.updateByPrimaryKeySelective(pendingAddress);
//        userAddressMapper.updateByPrimaryKey()  用这方法里面如果有空的属性会覆盖原来的内容的
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteUserAddress(String userId, String addressId) {
        UserAddress address = new UserAddress();
        address.setId(userId);
        address.setUserId(addressId);

        /** 通过一个对象去删除 */
        userAddressMapper.delete(address);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateUserAddressToBeDefault(String userId, String addressId) {
        // 1:查找默认地址，这只为不默认
        UserAddress queryAddress = new UserAddress();
        queryAddress.setUserId(userId);
        queryAddress.setIsDefault(YesOrNo.YES.type);
        List<UserAddress> list = userAddressMapper.select(queryAddress);
        /**
         通过一个for循环的目的：
           万一我们的数据库里面的数据出现紊乱， 一个用户可能出现多个的默认地址
           通过一个for循环可以把其他默认修改为非默认，这是一种保险的措施

         如果不使用for循环，单独使用一个对象去搜索的也可以，查询出一个单独的对象
         设置成 No.type
         */
        for (UserAddress ua: list) {
            ua.setIsDefault(YesOrNo.NO.type);
            userAddressMapper.updateByPrimaryKeySelective(ua);
        }
        // 2：根据地址id修改为默认地址
        UserAddress defaultAddress = new UserAddress();
        defaultAddress.setId(addressId);
        defaultAddress.setUserId(userId);
        defaultAddress.setIsDefault(YesOrNo.YES.type);

        // 更新
        userAddressMapper.updateByPrimaryKeySelective(defaultAddress);
    }



    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public UserAddress queryUserAddress(String userId, String addressId) {
        UserAddress singleAddress = new UserAddress();
        singleAddress.setId(addressId);
        singleAddress.setUserId(userId);

        return userAddressMapper.selectOne(singleAddress);
    }
}
