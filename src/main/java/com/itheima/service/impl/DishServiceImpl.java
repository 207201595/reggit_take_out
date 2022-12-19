package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomServiceException;
import com.itheima.dto.DishDto;
import com.itheima.mapper.DishMapper;
import com.itheima.pojo.Category;
import com.itheima.pojo.Dish;
import com.itheima.pojo.DishFlavor;
import com.itheima.pojo.SetmealDish;
import com.itheima.service.CategoryService;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import com.itheima.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/10 - 10 - 10 - 23:28
 */
@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper,Dish> implements DishService{
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SetmealDishService setmealDishService;

    @Value("${reggie.path}")
    private String imgPath;



    @Override
    public Page<DishDto> page(Integer currentPage, Integer pageSize, String name) {
        Page<Dish> dishPage = new Page<>(currentPage,pageSize);
        /**
         * 模糊查询dish菜品表
         * 根据更新时间排序
         */
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.like(Strings.isNotEmpty(name),Dish::getName,name);
        dishLambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);
        dishMapper.selectPage(dishPage, dishLambdaQueryWrapper);
        /**
         * 进行对象拷贝BeanUtils.copyProperties
         * 第一个参数：准备的拷贝对象
         * 第二个参数：拷贝到的对象
         * 第三个参数：忽略的属性
         */
        Page<DishDto> dishDtoPage = new Page<>(currentPage,pageSize);
        BeanUtils.copyProperties(dishPage,dishDtoPage,"records");
        //处理records的数据
        List<Dish> oldRecords = dishPage.getRecords();
        List<DishDto> newRecords = oldRecords.stream().map(dish -> {
            DishDto dishDto = new DishDto();
            //拷贝dish的普通属性
            BeanUtils.copyProperties(dish, dishDto);
            //获得分类ID
            Long categoryId = dish.getCategoryId();
            //根据查询分类对象
            Category category = categoryService.getById(categoryId);
            /**
             * 把category转换成Optional对象
             * 如果Optional不为空就设置dishDto的categoryName字段
             * 等同于下面的代码
             */
            Optional.of(category).ifPresent(c1 -> {
                dishDto.setCategoryName(c1.getName());
            });
//            if (category!=null){
//                String categoryName = category.getName();
//                dishDto.setCategoryName(categoryName);
//            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(newRecords);

        return dishDtoPage;
    }



    /**
     * 新增菜品同时保存口味数据
     * @param dishDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithFlavor(DishDto dishDto) {
        //清理redis缓存数据
        String key = "dish:"+dishDto.getCategoryId();
        stringRedisTemplate.delete(key);

        //保存菜品的基本信息到菜品表
        this.save(dishDto);
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((flavor) -> {
            flavor.setDishId(dishId);
            return flavor;
        }).collect(Collectors.toList());
        /**
         * 保存菜品口味
         * saveBatch  批量插入数据
         */
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据口味id 查询菜品信息和口味信息
     * @param id
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        DishDto dishDto = new DishDto();

        /**
         * 查询菜品信息和查询菜品口味信息
         */
        Dish dish = dishMapper.selectById(id);
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> list = dishFlavorService.list(dishFlavorLambdaQueryWrapper);

        /**
         * 把菜品信息拷贝到dishDto对象
         */
        BeanUtils.copyProperties(dish,dishDto);
        dishDto.setFlavors(list);

        return dishDto;
    }

    /**
     * 修改菜品表 和菜品口味表
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //清理redis缓存数据
        String key = "dish:"+dishDto.getCategoryId();
        stringRedisTemplate.delete(key);
        /**
         * 更新dish菜品表
         */
        dishMapper.updateById(dishDto);

        /**
         * 更新菜品口味表
         * 由于菜品口味可能会减少 单纯的修改无法解决
         * 我们可以先清理当前的菜品口味表 再重新添加
         */
        //清理口味表
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(dishFlavorLambdaQueryWrapper);
        //重新添加口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        /**
         * 把每个口味的dishId设置一下
         */
        flavors = flavors.stream().map(flavor -> {
            flavor.setDishId(dishDto.getId());
            return flavor;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 删除菜品和菜品口味信息
     * @param ids
     */
    @Override
    public void delete(String ids) {
        //得到所有的菜品id
        String[] split = ids.split(",");
        List<Long> list = Arrays.stream(split).map(id -> Long.valueOf(id)).collect(Collectors.toList());
        //stream流过滤器判断当前菜品是否包含在售的
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId,list).eq(Dish::getStatus,1);
        long count = super.list(dishLambdaQueryWrapper).stream().count();
        if (count>0){
            //如果不能删除 抛出异常
            throw new CustomServiceException("有正在售卖的菜品，不能删除，请先停售");
        }
        //判断当前菜品是否有没有在套餐中的
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getDishId,list);
        long setmealDishCount = setmealDishService.list(setmealDishLambdaQueryWrapper).stream().count();
        if (setmealDishCount>0){
            throw new CustomServiceException("有套餐包含当前菜品，请先删除套餐");
        }
        //操作清空一下redis相关的缓存数据
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>().in(Dish::getId, ids);
        List<Dish> dishList = super.list(queryWrapper);
        List<String> redisDeleteKeys = dishList.stream().map(dish -> {
            return "dish:" + dish.getCategoryId();
        }).collect(Collectors.toList());
        stringRedisTemplate.delete(redisDeleteKeys);

        /**
         * 删除菜品和菜品口味数据 以及菜品对应的图片信息
         */
        LambdaQueryWrapper<Dish> imgQueryWrapper = new LambdaQueryWrapper<>();
        imgQueryWrapper.in(Dish::getId,list);
        List<String> imgList = super.list(imgQueryWrapper).stream().map(dish -> {
            return dish.getImage();
        }).collect(Collectors.toList());

        long deleteImgCount = imgList.stream().filter(imgName -> {
            File file = new File(imgPath + imgName);
            return file.delete();
        }).count();
        System.out.println("删除了"+deleteImgCount+"张图片");
        super.removeBatchByIds(list);
        dishFlavorService.removeBatchByIds(list);

    }


}
