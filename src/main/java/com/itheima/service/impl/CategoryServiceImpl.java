package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomServiceException;
import com.itheima.mapper.CategoryMapper;
import com.itheima.pojo.Category;
import com.itheima.pojo.Dish;
import com.itheima.pojo.Setmeal;
import com.itheima.service.CategoryService;
import com.itheima.service.DishService;
import com.itheima.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/8 - 10 - 08 - 02:02
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    @Override
    public Page<Category> CATEGORY_PAGE(Integer currentPage, Integer pageSize) {

        Page<Category> categoryIPage = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.orderByAsc(Category::getSort);
        categoryMapper.selectPage(categoryIPage,categoryLambdaQueryWrapper);
        return categoryIPage;
    }

    @Override
    public boolean selectAndRemove(Long id) {
        //查询是否关联了菜品 如果关联就抛出业务异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();

        dishLambdaQueryWrapper.select(Dish::getCategoryId).eq(Dish::getCategoryId,id);
        long dishCount = dishService.count(dishLambdaQueryWrapper);

        if (dishCount>0){
            //已经关联菜品 抛出异常
            throw new CustomServiceException("当前分类关联了菜品不能删除");
        }
        //查询是否关联了套餐 如果关联就抛出业务异常

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.select(Setmeal::getCategoryId).eq(Setmeal::getCategoryId,id);
        long setmealCount = setmealService.count(setmealLambdaQueryWrapper);
        if (setmealCount>0){
            //已经关联套餐 抛出异常
            throw new CustomServiceException("当前分类关联了套餐不能删除");
        }
        return super.removeById(id);
    }
}
