package com.music.rental.config;

import com.music.rental.common.BizException;
import com.music.rental.common.R;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 全局异常处理：业务异常返回 500 + 中文提示，参数校验返回首条错误 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public R<Void> handleBiz(BizException e) {
        return R.fail(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst().map(f -> f.getField() + " " + f.getDefaultMessage()).orElse("参数校验失败");
        return R.fail(msg);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public R<Void> handleDup(DuplicateKeyException e) {
        return R.fail("数据重复，请检查编号或账号是否已存在");
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleOther(Exception e) {
        e.printStackTrace();
        return R.fail("系统异常：" + e.getMessage());
    }
}
