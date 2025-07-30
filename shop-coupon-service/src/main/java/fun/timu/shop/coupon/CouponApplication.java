package fun.timu.shop.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan(basePackages = "fun.timu.shop.coupon.mapper")
@ComponentScan(basePackages = {"fun.timu.shop.coupon", "fun.timu.shop.common"})
@EnableFeignClients(basePackages = "fun.timu.shop.coupon.feign")
public class CouponApplication {
    public static void main(String[] args) {
        SpringApplication.run(CouponApplication.class, args);

    }
}
