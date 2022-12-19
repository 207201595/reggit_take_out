package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomServiceException;
import com.itheima.dto.SetmealDto;
import com.itheima.mapper.SetmealMapper;
import com.itheima.pojo.Setmeal;
import com.itheima.pojo.SetmealDish;
import com.itheima.service.CategoryService;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/10 - 10 - 10 - 23:31
 */
@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${reggie.path}")
    private String imgPath;


    /**
     * 保存套餐 和 套餐菜品关系数据
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //清空redis数据
        String key = "setmeal:"+setmealDto.getCategoryId();
        stringRedisTemplate.delete(key);
        /**
         * 先保存套餐相关信息
         * 当执行完保存后 空的id会被框架赋值
         */
        super.save(setmealDto);
        /**
         * 填充套餐和菜品关系表的 setmealId字段
         */
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes = setmealDishes.stream().map(setmealDish -> {
            setmealDish.setSetmealId(setmealDto.getId());
            return setmealDish;
        }).collect(Collectors.toList());
        /**
         * 保存到套餐和菜品关系表
         */
        setmealDishService.saveBatch(setmealDishes);

    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    public Page<SetmealDto> page(Integer page, Integer pageSize,String name) {
        Page<Setmeal> setmealPage = new Page<>();
        /**
         * 添加模糊查询条件
         */
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.like(name!=null,Setmeal::getName,name);
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        /**
         * 执行分页查询
         */
        setmealMapper.selectPage(setmealPage, setmealLambdaQueryWrapper);
        /**
         * 原有的分页数据缺少套餐分类名称
         * 我们使用SetmealDto解决
         */
        Page<SetmealDto> setmealDtoPage = new Page<>();
        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");

        List<Setmeal> oldRecords = setmealPage.getRecords();
        List<SetmealDto> newRecords = oldRecords.stream().map(setmeal -> {
            /**
             * 把现有的数据拷贝到dto
             */
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal, setmealDto);
            /**
             * 查询需要的套餐分类名称
             */
            String categoryName = categoryService.getById(setmeal.getCategoryId()).getName();
            setmealDto.setCategoryName(categoryName);

            return setmealDto;
        }).collect(Collectors.toList());

        /**
         * 给新的Records 赋值
         */
        setmealDtoPage.setRecords(newRecords);

        return setmealDtoPage;
    }

    /**
     * 批量删除
     * @param ids
     */
    @Override
    public void delete(String ids) {
        //把字符串中的id提取成数组
        String[] setmealIds = ids.split(",");
        /**
         *  将String类型的数组转换成Long类型的集合
         */
        List<Long> list = Arrays.stream(setmealIds).map(setmealId -> {
            return Long.valueOf(setmealId);
        }).collect(Collectors.toList());
        /**
         * 查询套餐状态 看是否可以删除
         */
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId,list).eq(Setmeal::getStatus,1);
        long count = super.count(setmealLambdaQueryWrapper);
        if (count>0){
            //如果不能删除 抛出异常
            throw new CustomServiceException("有正在售卖中的套餐，不能删除");
        }
        /**
         * 如果没有抛出异常 可以删除
         */
        //删除套餐对应的图片
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,list);
        List<Setmeal> setmeals = super.list(queryWrapper);
        //清空redis的数据
        List<String> redisDeleteKeys = setmeals.stream().map(setmeal -> {
            return "setmeal:" + setmeal.getCategoryId();
        }).collect(Collectors.toList());
        stringRedisTemplate.delete(redisDeleteKeys);
        //删除对应的图片
        List<String> imgList = setmeals.stream().map(setmeal -> {
            return setmeal.getImage();
        }).collect(Collectors.toList());

        long deleteImgCount = imgList.stream().filter(imgName -> {
            File file = new File(imgPath + imgName);
            return file.delete();
        }).count();
        System.out.println("删除了"+deleteImgCount+"张图片");
        //删除套餐表
        super.removeBatchByIds(list);
        //删除套餐关系表
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId,list);

        setmealDishService.remove(setmealDishLambdaQueryWrapper);

    }

    /**
     * 修改套餐
     * @param setmealDto
     */
    @Override
    public void updateWithDish(SetmealDto setmealDto) {

        //清空redis数据
        String key = "setmeal:"+setmealDto.getCategoryId();
        stringRedisTemplate.delete(key);
        //先修改套餐修改信息
        super.updateById(setmealDto);
        /**
         * 修改套餐和菜品关系的信息
         * 先清空原有的关系信息
         * 再重新保存 避免信息残留
         */
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
        //设置菜品id后 再进行批量保存
        List<SetmealDish> list = setmealDto.getSetmealDishes().stream().map(setmealDish -> {
            setmealDish.setSetmealId(setmealDto.getId());
            return setmealDish;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(list);

    }
}
