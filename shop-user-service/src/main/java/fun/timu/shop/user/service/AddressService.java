package fun.timu.shop.user.service;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.user.controller.request.AddressAddReqeust;
import fun.timu.shop.user.model.DO.AddressDO;
import com.baomidou.mybatisplus.extension.service.IService;
import fun.timu.shop.user.model.VO.AddressVO;

import java.util.List;

/**
 * @author zhengke
 * @description 针对表【address(电商-公司收发货地址表)】的数据库操作Service
 * @createDate 2025-07-25 10:19:00
 */
public interface AddressService {

    /**
     * 查找指定地址详情
     *
     * @param id
     * @return
     */
    AddressVO detail(Long id);

    /**
     * 新增收货地址
     *
     * @param addressAddReqeust
     */
    JsonData add(AddressAddReqeust addressAddReqeust);

    /**
     * 根据id删除地址
     *
     * @param addressId
     * @return
     */
    JsonData del(Long addressId) throws Exception;

    /**
     * 查找用户全部收货地址
     *
     * @return
     */
    List<AddressVO> listUserAllAddress();
}
