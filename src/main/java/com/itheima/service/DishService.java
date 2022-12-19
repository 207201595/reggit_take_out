package com.itheima.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.dto.DishDto;
import com.itheima.pojo.Dish;
import org.springframework.stereotype.Service;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/10 - 10 - 10 - 23:27
 */
@Service
public interface DishService extends IService<Dish> {

    /**
     * 菜品分页数据
     */
    Page<DishDto> page(Integer currentPage,Integer pageSize,String name);

    /**
     * 新增菜品同时插入对应的菜品口味数据
     * 需要同时操作两张表 Dish 和DishFlavor
     */

    public void  saveWithFlavor(DishDto dishDto);

    /**
     * 查询菜品和口味信息
     */
    public DishDto getByIdWithFlavor(Long id);

    /**
     * 修改菜品表
     */
    public void updateWithFlavor(DishDto dishDto);

    /**
     *
     * 删除方法
     */
    public void delete(String ids);
}
