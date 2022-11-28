package com.itheima.common;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/6 - 10 - 06 - 22:23
 */

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 全局异常处理器
     * @param exception
     * @return
     */
    @ExceptionHandler(java.sql.SQLIntegrityConstraintViolationException.class)
    public Result<String> exceptionHandler(SQLIntegrityConstraintViolationException exception){
        log.error(exception.getMessage());
        /**
         * 如果sql异常包含重复条目关键字
         * 就处理成xxx已存在
         */
        if (exception.getMessage().contains("Duplicate entry")){
            String[] split = exception.getMessage().split(" ");
            val s = split[2] + "已存在";
            return Result.error(s);
        }
        return Result.error("未知错误，联系管理员");

    }

    /**
     * 全局业务异常处理器
     * @param customServiceException
     * @return
     */
    @ExceptionHandler(CustomServiceException.class)
    public Result<String> CategoryExceptionHandler(CustomServiceException customServiceException){
        log.error(customServiceException.getMessage());
        return Result.error(customServiceException.getMessage());
    }
}
