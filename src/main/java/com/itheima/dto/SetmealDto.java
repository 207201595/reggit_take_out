package com.itheima.dto;


import com.itheima.pojo.Setmeal;
import com.itheima.pojo.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {
    /**
     * 套餐菜品关系表
     */
    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
