package fun.timu.shop.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan(basePackages = "fun.timu.shop.user.mapper")
@ComponentScan(basePackages = {"fun.timu.shop.user", "fun.timu.shop.common"})
@EnableFeignClients(basePackages = "fun.timu.shop.user.feign")
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
