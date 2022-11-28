package com.itheima.controller;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/11 - 10 - 11 - 22:57
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.Result;
import com.itheima.dto.DishDto;
import com.itheima.pojo.Dish;
import com.itheima.pojo.DishFlavor;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品口味和菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 查询分页数据
     */
    @GetMapping("/page")
    public Result<Page> getDish(Integer page, Integer pageSize, String name){
        return Result.success(dishService.page(page,pageSize,name));
    }

    /**
     * 新增菜品方法
     * @param dishDto
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);

        return Result.success("新增菜品成功");
    }
    /**
     * 查询修改菜品的回显信息
     */
    @GetMapping("/{id}")
    public Result<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return Result.success(dishDto);
    }
    /**
     * 修改菜品
     */
    @PutMapping
    public Result<String> put(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);

        return Result.success("修改菜品成功");
    }
    /**
     * 根据CategoryId分类id查询菜品数据
     * 这里为了满足H5端需要菜品口味信息 所以使用DishDto作为返回值
     */
    @GetMapping("/list")
    public Result<List<DishDto>> list(Dish dish){

        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (dish.getCategoryId()!=null){
            dishLambdaQueryWrapper.eq(Dish::getCategoryId,dish.getCategoryId());
        }
        /**
         * 添加菜品的关键词查询条件
         */
        if (dish.getName()!=null){
            dishLambdaQueryWrapper.like(Dish::getName,dish.getName());
        }
        /**
         * 只查询启售状态的
         * 0 停售 1 启售
         */
        dishLambdaQueryWrapper.eq(Dish::getStatus,1);
        /**
         * 先根据sort字段排序
         * 再根据修改时间排序
         */
        dishLambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> dishList = dishService.list(dishLambdaQueryWrapper);

        /**
         * 原有的dish集合中的菜品 不包含菜品口味信息
         * 使用dishDto来解决
         */
        List<DishDto> dishDtoList = dishList.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, item.getId());
            List<DishFlavor> dishFlavors = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());

        return Result.success(dishDtoList);
    }



}
