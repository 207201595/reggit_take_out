package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.common.Result;
import com.itheima.common.utils.MyThreadLocalUtil;
import com.itheima.pojo.ShoppingCart;
import com.itheima.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Auther: zhangTZ
 * @Date: 2022/11/17 - 11 - 17 - 23:31
 */
@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 往购物车添加数据
     *
     * 同时也是购物车添加份数的方法
     * @return
     */
    @PostMapping("/add")
    @Transactional
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart, HttpSession session){
        //设置用户id
        Long userId = (Long) session.getAttribute("user");
        shoppingCart.setUserId(userId);

        /**
         * 查询当前套餐或者菜品是否已经添加到购物车了
         * 如果已经存在 在原来的数量基础上加1
         */
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件  当前用户下的菜品
        queryWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId());

        if (shoppingCart.getDishId()!=null){
            //添加的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }
        if (shoppingCart.getSetmealId()!=null){
            //添加的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart shoppingCartOne = shoppingCartService.getOne(queryWrapper);

        if (shoppingCartOne!=null){ //如果已经存在 就数量加1
            shoppingCartOne.setNumber(shoppingCartOne.getNumber()+1);
            shoppingCartService.updateById(shoppingCartOne);

        }else {
            //不存在添加到购物车 并且数量默认就是1
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
            shoppingCartOne = shoppingCart;
        }

        return Result.success(shoppingCartOne);
    }

    /**
     * 购物车的数据
     * @return
     */
//    @GetMapping("/list")
//    public Result<List<ShoppingCart>> list(HttpSession session){
//        String userId = session.getAttribute("user").toString();
//        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(ShoppingCart::getUserId,userId);
//        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
//        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
//        return Result.success(list);
//    }

    /**
     * 上面使用session获取  也可以使用ThreadLocal的线程副本副本获取
     * @param
     * @return
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(){
        log.info("查询购物车");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        /**
         *  MyThreadLocalUtil.getId()
         *  通过封装的工具类获取
         */
        queryWrapper.eq(ShoppingCart::getUserId, MyThreadLocalUtil.getId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return Result.success(list);
    }

    /**
     * 购物车减去份数
     */
    @PostMapping("/sub")
    @Transactional
    public Result<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        LambdaQueryWrapper<ShoppingCart>  queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,MyThreadLocalUtil.getId());
        //判断是套餐还是菜品
        if (shoppingCart.getDishId()!=null){
            //菜品
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }
        if (shoppingCart.getSetmealId()!=null){
            //套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        shoppingCart = shoppingCartService.getOne(queryWrapper);
        /**
         * 如果数量大于1 就减去1
         * 小于1 就删除
         */
        Integer number = shoppingCart.getNumber();
        if (number>1){
           shoppingCart.setNumber(number-1);
           shoppingCartService.updateById(shoppingCart);
       }else {
            shoppingCartService.remove(queryWrapper);
        }
        return Result.success(shoppingCart);
    }
    /**
     * 清空购物车
     */
    @DeleteMapping("/clean")
    public Result<String> clean(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,MyThreadLocalUtil.getId());

        shoppingCartService.remove(queryWrapper);

        return Result.success("清空成功");
    }
    
}
