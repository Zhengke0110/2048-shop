package fun.timu.shop.coupon.controller;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.coupon.service.CouponRecordService;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/coupon/v1/record")
@Validated
@AllArgsConstructor
public class CouponRecordController {
    private final CouponRecordService couponRecordService;

    /**
     * 分页查询用户优惠券记录
     *
     * @param page 页码，从1开始
     * @param size 每页大小，最大100
     * @return 分页数据
     */
    @GetMapping("/page")
    public JsonData page(@RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") int page,
                         @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") int size) {
        log.info("分页查询优惠券记录: page={}, size={}", page, size);
        return couponRecordService.page(page, size);
    }
}
