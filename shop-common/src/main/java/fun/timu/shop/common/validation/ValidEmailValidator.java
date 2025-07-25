package fun.timu.shop.common.validation;

import fun.timu.shop.common.util.CheckUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 邮箱格式校验器
 * 使用 CheckUtil 工具类进行验证
 */
public class ValidEmailValidator implements ConstraintValidator<ValidEmail, String> {

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        // 初始化方法，无需特殊处理
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null 值由 @NotNull 或 @NotBlank 处理
        if (value == null) {
            return true;
        }
        
        return CheckUtil.isEmail(value);
    }
}
