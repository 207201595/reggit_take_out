package com.itheima;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@Slf4j
@ServletComponentScan
@EnableTransactionManagement
public class ReggitTakeOutApplication {


    public static void main(String[] args) {
        SpringApplication.run(ReggitTakeOutApplication.class, args);
        log.info("项目启动成功");
        log.info("测试push成功");


    }


}
