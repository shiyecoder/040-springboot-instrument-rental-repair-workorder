package com.music.rental.common;

/** 业务异常：由全局异常处理器转成统一返回结构 */
public class BizException extends RuntimeException {
    public BizException(String message) {
        super(message);
    }
}
