package fun.timu.shop.common.exception;

import fun.timu.shop.common.util.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletRequest;
import java.util.StringJoiner;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(value = BizException.class)
    @ResponseBody
    public JsonData handleBizException(BizException e, HttpServletRequest request) {
        log.warn("[业务异常] 请求地址: {}, 异常信息: {}", request.getRequestURI(), e.getMsg());
        return JsonData.buildCodeAndMsg(e.getCode(), e.getMsg());
    }

    /**
     * 处理参数校验异常 - @Valid 和 @Validated
     */
    @ExceptionHandler(value = {MethodArgumentNotValidException.class, BindException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public JsonData handleValidationException(Exception e, HttpServletRequest request) {
        StringJoiner errorMsg = new StringJoiner(", ");
        
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException validException = (MethodArgumentNotValidException) e;
            for (FieldError fieldError : validException.getBindingResult().getFieldErrors()) {
                errorMsg.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
            }
        } else if (e instanceof BindException) {
            BindException bindException = (BindException) e;
            for (FieldError fieldError : bindException.getBindingResult().getFieldErrors()) {
                errorMsg.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
            }
        }
        
        String message = "参数校验失败: " + errorMsg.toString();
        log.warn("[参数校验异常] 请求地址: {}, 错误信息: {}", request.getRequestURI(), message);
        return JsonData.buildError(message);
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public JsonData handleException(Exception e, HttpServletRequest request) {
        log.error("[系统异常] 请求地址: {}, 异常类型: {}, 异常信息: {}", 
                  request.getRequestURI(), e.getClass().getSimpleName(), e.getMessage(), e);
        return JsonData.buildError("系统繁忙，请稍后重试");
    }

}
