package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pojo.Dish;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/10 - 10 - 10 - 23:26
 */
@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
