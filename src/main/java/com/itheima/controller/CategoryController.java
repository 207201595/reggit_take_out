package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.Result;
import com.itheima.pojo.Category;
import com.itheima.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/8 - 10 - 08 - 02:06
 */

/**
 * 分类管理
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 查询分类的分页数据
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Result<Page> Page(Integer page,Integer pageSize){
        log.info("分页数据为"+page+pageSize);
        Page<Category> categoryPage = categoryService.CATEGORY_PAGE(page, pageSize);
        return Result.success(categoryPage);
    }

    /**
     * 新增菜品分类和新增套餐分类
     * @param category
     * @return
     */
    @PostMapping
    public Result<String> add(@RequestBody Category category){
        boolean save = categoryService.save(category);
        if (save){
            return Result.success("添加成功");
        }
        return Result.success("添加失败");
    }

    /**
     * 分类删除的方法
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> delete(Long ids){
        categoryService.selectAndRemove(ids);
        return Result.success("分类信息删除成功");
    }

    /**
     * 修改的方法
     * @param category
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody Category category){
        log.info(category.getName());
        log.info("id {}",category.getId());
        log.info("sort {}",category.getSort());
        boolean save = categoryService.updateById(category);
        if (save){
            return Result.success("修改成功");
        }
        return Result.error("修改失败");
    }

    /**
     * 根据条件查询分类数据
     * @param category
     * @return
     */
    @GetMapping("/list")
    public Result<List<Category>> list(Category category){
        /**
         * 添加查询条件
         */
        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        /**
         * 根据sort正序排序  再根据更新时间倒序排序
         */
        categoryLambdaQueryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //执行查询
        List<Category> list = categoryService.list(categoryLambdaQueryWrapper);
        return Result.success(list);

    }
}
