package com.itheima.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.dto.SetmealDto;
import com.itheima.pojo.Setmeal;
import org.springframework.stereotype.Service;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/10 - 10 - 10 - 23:30
 */
@Service
public interface SetmealService extends IService<Setmeal> {

    public void saveWithDish(SetmealDto setmealDto);

    public Page<SetmealDto> page(Integer page,Integer pageSize,String name);

    public void delete(String ids);

    public void updateWithDish(SetmealDto setmealDto);
}
