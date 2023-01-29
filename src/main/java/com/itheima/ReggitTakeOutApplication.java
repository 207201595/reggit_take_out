package com.itheima;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@Slf4j
@ServletComponentScan
@EnableTransactionManagement    //开启事务式注解驱动
@EnableCaching //开启注解式缓存功能
public class ReggitTakeOutApplication {
    //取消空闲时间过久的连接进行注销报出的错误提示
    static {
        System.setProperty("druid.mysql.usePingMethod", "false");
    }

    public static void main(String[] args) {
        SpringApplication.run(ReggitTakeOutApplication.class, args);
        log.info("瑞吉项目启动成功");

    }


}
