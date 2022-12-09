package com.itheima.common;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/11 - 10 - 11 - 00:11
 */

/**
 * 自定义异常CustomServiceException
 * 测试推送
 */

public class CustomServiceException extends RuntimeException{

    public CustomServiceException(String message) {
        super(message);
    }

    public CustomServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
