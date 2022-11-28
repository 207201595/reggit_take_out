package com.itheima.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.pojo.Category;
import org.springframework.stereotype.Service;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/8 - 10 - 08 - 02:02
 */
@Service
public interface CategoryService extends IService<Category> {
    /**
     * 分页
     * @param currentPage
     * @param pageSize
     * @return
     */
    Page<Category> CATEGORY_PAGE(Integer currentPage, Integer pageSize);

    /**
     * 根据id删除菜品分类
     * 删除前先验证当前菜品有没有绑定套餐或者菜品
     * @param id
     */
    boolean selectAndRemove(Long id);
}
