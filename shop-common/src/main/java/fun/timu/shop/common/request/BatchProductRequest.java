package fun.timu.shop.common.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 批量获取商品详情请求类
 *
 * @author zhengke
 */
@Data
public class BatchProductRequest {
    
    /**
     * 商品ID列表
     */
    @JsonProperty("product_ids")
    private List<Long> productIds;
}
