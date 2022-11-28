package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.pojo.Orders;
import org.springframework.stereotype.Service;

/**
 * @Auther: zhangTZ
 * @Date: 2022/11/22 - 11 - 22 - 07:29
 */
@Service
public interface OrderService extends IService<Orders> {
    /**
     * 用户下单
     * @param orders
     */
    void submit(Orders orders);
}
