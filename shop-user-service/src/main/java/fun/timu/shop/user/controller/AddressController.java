package fun.timu.shop.user.controller;

import fun.timu.shop.common.enums.BizCodeEnum;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.user.controller.request.AddressAddReqeust;
import fun.timu.shop.user.model.VO.AddressVO;
import fun.timu.shop.user.service.AddressService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address/v1/address")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    /**
     * 新增收货地址
     *
     * @param addressAddReqeust
     * @return
     */
    @PostMapping("add")
    public JsonData add(@RequestBody AddressAddReqeust addressAddReqeust) {
        return addressService.add(addressAddReqeust);
    }

    /**
     * 获取收货地址详情
     *
     * @param addressId
     * @return
     */
    @GetMapping("/find/{address_id}")
    public Object detail(@PathVariable("address_id") long addressId) {
        AddressVO addressVO = addressService.detail(addressId);
        return addressVO == null ? JsonData.buildResult(BizCodeEnum.ADDRESS_NO_EXITS) : JsonData.buildSuccess(addressVO);
    }

    /**
     * 删除收货地址
     *
     * @param addressId
     * @return
     */
    @DeleteMapping("/del/{address_id}")
    public JsonData del(@PathVariable("address_id") int addressId) {
        return addressService.del(addressId);
    }

    /**
     * 获取用户所有收货地址
     *
     * @return
     */
    @GetMapping("/list")
    public JsonData findUserAllAddress() {
        List<AddressVO> list = addressService.listUserAllAddress();
        return JsonData.buildSuccess(list);
    }


}
