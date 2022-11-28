package com.itheima.controller;

/**
 * @Auther: zhangTZ
 * @Date: 2022/11/2 - 11 - 02 - 22:46
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.Result;
import com.itheima.dto.SetmealDto;
import com.itheima.pojo.Setmeal;
import com.itheima.pojo.SetmealDish;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SetmealDish 和Setmeal共同的前端控制器
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody SetmealDto setmealDto) {

        setmealService.saveWithDish(setmealDto);
        return Result.success("新增套餐成功");
    }
    /**
     * 分页查询套餐数据
     */
    @GetMapping("/page")
    public Result<Page<SetmealDto>> page(Integer page,Integer pageSize,Setmeal setmeal){
        Page<SetmealDto> setmealDtoPage = setmealService.page(page, pageSize,setmeal.getName());

        return  Result.success(setmealDtoPage);
    }
    /**
     * 删除套餐
     * 参数是1590310805745999873,1590310805745999774这种的
     * 可以使用list集合直接接收到
     * 也可以先用String接收再转换
     */
//    @DeleteMapping
//    public Result<String> delete(@RequestParam List<long> ids){
//
//    }
    @DeleteMapping
    public Result<String> delete(String ids){
        setmealService.delete(ids);
        return Result.success("删除成功");
    }

    /**
     * 启售和停售套餐
     */
    @PostMapping("/status/{status}")
    public Result<String> updateStatus(@PathVariable Integer status,@RequestParam List<Long> ids ){
        System.out.println(status);
        ids.forEach(System.out::println);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        List<Setmeal> list = setmealService.list(queryWrapper);
        list = list.stream().map(setmeal -> {
            setmeal.setStatus(status);
            return setmeal;
        }).collect(Collectors.toList());

        setmealService.updateBatchById(list);

        return Result.success("套餐状态修改成功");
    }
    /**
     * 修改套餐前查询
     */
    @GetMapping("/{id}")
    public Result<SetmealDto> updateSelect(@PathVariable Long id){
        Setmeal setmeal = setmealService.getById(id);
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> setmealDishes = setmealDishService.list(setmealDishLambdaQueryWrapper);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);
        setmealDto.setSetmealDishes(setmealDishes);
        return Result.success(setmealDto);
    }
    /**
     * 保存修改的套餐
     */
    @PutMapping
    public Result<String> update(@RequestBody SetmealDto setmealDto) {

        setmealService.updateWithDish(setmealDto);
        return Result.success("修改套餐成功");
    }
    /**
     * 手机端 查询套餐数据
     */
    @GetMapping("/list")
    public Result<List<Setmeal>> list(Setmeal setmeal){
        System.out.println(setmeal);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);

        return Result.success(list);
    }
}
