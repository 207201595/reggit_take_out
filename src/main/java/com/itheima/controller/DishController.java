package com.itheima.controller;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/11 - 10 - 11 - 22:57
 */

import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.itheima.common.JacksonObjectMapper;
import com.itheima.common.Result;
import com.itheima.dto.DishDto;
import com.itheima.pojo.Dish;
import com.itheima.pojo.DishFlavor;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JacksonObjectMapper jacksonObjectMapper;



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
     * 删除菜品的方法
     */
    @DeleteMapping
    public Result<String> delete(String ids){
        dishService.delete(ids);
        return Result.success("删除成功");
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
    public Result<List<DishDto>> list(Dish dish) throws JsonProcessingException {
        List<DishDto> dishDtoList = null;
                //动态设置redis的key
        String key = "dish:"+dish.getCategoryId();
        //先存redis中获取缓存数据
        String s = stringRedisTemplate.opsForValue().get(key);
        System.out.println("从redis获取的值为"+s);
        //如果从redis查到数据直接返回
        if (s!=null){
            //反序列化为List<DishDto>类型
            dishDtoList= jacksonObjectMapper.readValue(s, new TypeReference<List<DishDto>>() {
            });
//            dishDtoList = JSON.parseObject(s, new TypeReference<List<DishDto>>() {
//            });
            return Result.success(dishDtoList);
        }
        //不存在查询mysql数据库
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
        dishDtoList = dishList.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, item.getId());
            List<DishFlavor> dishFlavors = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());
        /**
         * 序列化 然后存储到redis中 设置超时时间为60分钟
         */
        String selectJson = jacksonObjectMapper.writeValueAsString(dishDtoList);
//        String selectJson = JSON.toJSONString(dishDtoList);
        stringRedisTemplate.opsForValue().set(key,selectJson,60, TimeUnit.MINUTES);
        return Result.success(dishDtoList);
    }

    /**
     * 批量停售启售菜品
     */
    @PostMapping("/status/{status}")
    public Result<String> status(@PathVariable Integer status,@RequestParam List<Long> ids){

        //先得到套餐数据
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids);
        List<Dish> list = dishService.list(queryWrapper);
        //清空redis相关数据
        List<String> redisDeleteKeys = list.stream().map(dish -> {
            return "dish:" + dish.getCategoryId();
        }).collect(Collectors.toList());
        stringRedisTemplate.delete(redisDeleteKeys);
        //修改套餐数据
        list = list.stream().map(dish -> {
            dish.setStatus(status);
            return dish;
        }).collect(Collectors.toList());
        //修改数据库表
        dishService.updateBatchById(list);

        return Result.success("修改成功");
    }



}
