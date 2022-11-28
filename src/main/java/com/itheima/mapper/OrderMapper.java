package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pojo.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Auther: zhangTZ
 * @Date: 2022/11/22 - 11 - 22 - 07:28
 */
@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
