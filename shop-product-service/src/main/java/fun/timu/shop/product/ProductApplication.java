package fun.timu.shop.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan(basePackages = "fun.timu.shop.product.mapper")
@ComponentScan(basePackages = {"fun.timu.shop.product", "fun.timu.shop.common"})
@EnableFeignClients(basePackages = "fun.timu.shop.product.feign")
public class ProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
