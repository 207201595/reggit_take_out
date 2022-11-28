package com.itheima.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.Result;
import com.itheima.pojo.Orders;
import com.itheima.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Auther: zhangTZ
 * @Date: 2022/11/22 - 11 - 22 - 07:27
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Orders orders){
        log.info("提交的订单数据"+orders);
        orderService.submit(orders);
        return Result.success("等待收货");
    }

    /**
     * 查询订单信息
     */
    @GetMapping("/userPage")
    public Result<Page<Orders>> page(Integer page ,Integer pageSize){
        log.info("测试git版本切换");
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        orderService.page(ordersPage);
        return Result.success(ordersPage);
    }

}
