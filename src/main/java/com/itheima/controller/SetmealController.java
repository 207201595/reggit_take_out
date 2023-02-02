package com.itheima.controller;

/**
 * @Auther: zhangTZ
 * @Date: 2022/11/2 - 11 - 02 - 22:46
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.itheima.common.JacksonObjectMapper;
import com.itheima.common.Result;
import com.itheima.dto.SetmealDto;
import com.itheima.pojo.Setmeal;
import com.itheima.pojo.SetmealDish;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * SetmealDish 和Setmeal共同的前端控制器
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
@Api(tags = "套餐接口-SetmealController")
public class SetmealController {
    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

   @Autowired
   private JacksonObjectMapper jacksonObjectMapper;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    @ApiOperation(value = "新增套餐接口")
    @ApiImplicitParams({
            // required 是否必须
            @ApiImplicitParam(name = "setmealDto",value = "套餐明细",required = true)
    })
    public Result<String> save(@RequestBody SetmealDto setmealDto) {

        setmealService.saveWithDish(setmealDto);
        return Result.success("新增套餐成功");
    }
    /**
     * 分页查询套餐数据
     */
    @GetMapping("/page")
    @ApiOperation(value = "分类查询套餐接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",required = true),
            @ApiImplicitParam(name = "pageSize",value = "条目数",required = true),
            @ApiImplicitParam(name = "name",value = "套餐名称",required = false)
    })
    public Result<Page<SetmealDto>> page(Integer page,Integer pageSize,String name){
        Page<SetmealDto> setmealDtoPage = setmealService.page(page, pageSize,name);

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

    /**
     *  allEntries = true 表示删除所有的缓存数据
     * @param ids
     * @return
     */
//    @CacheEvict(value = "setmealCache",allEntries = true)
    @DeleteMapping
    @ApiOperation(value = "删除套餐接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids",value = "套餐id数组",required = true)
    })
    public Result<String> delete(String ids){
        setmealService.delete(ids);
        return Result.success("删除成功");
    }

    /**
     * 启售和停售套餐
     */
    @PostMapping("/status/{status}")
    @ApiOperation(value = "启售停售套餐接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status",value = "状态",required = true),
            @ApiImplicitParam(name = "ids",value = "套餐id集合",required = true)
    })
    public Result<String> updateStatus(@PathVariable Integer status,@RequestParam List<Long> ids ){
        //查询套餐数据
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        List<Setmeal> list = setmealService.list(queryWrapper);

        //清空一下redis数据
        List<String> redisDeleteKeys = list.stream().map(setmeal -> {
            return "setmeal:" + setmeal.getCategoryId();
        }).collect(Collectors.toList());
        stringRedisTemplate.delete(redisDeleteKeys);

        //修改所有的套餐状态
        list = list.stream().map(setmeal -> {
            setmeal.setStatus(status);
            return setmeal;
        }).collect(Collectors.toList());

        setmealService.updateBatchById(list);

        return Result.success("套餐状态修改成功");
    }
    /**
     * 修改套餐前查询 展示的数据
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "套餐查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "套餐id",required = true)
    })
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
    @ApiOperation(value = "保存修改套餐接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "setmealDto",value = "套餐信息",required = true)
    })
    public Result<String> update(@RequestBody SetmealDto setmealDto) {

        setmealService.updateWithDish(setmealDto);
        return Result.success("修改套餐成功");
    }
    /**
     * 手机端 查询套餐数据
     */
//    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+':'+#setmeal.status")
    @GetMapping("/list")
    @ApiOperation(value = "手机端-套餐条件查询接口")
    public Result<List<Setmeal>> list(Setmeal setmeal) throws JsonProcessingException {
        List<Setmeal> list = null;
//        先查询redis有没有缓存数据 如果没有查询数据库
        String key = "setmeal:"+setmeal.getCategoryId();
        String s = stringRedisTemplate.opsForValue().get(key);
        if (s!=null){
            //如果查询到数据就序列化后返回
            list = jacksonObjectMapper.readValue(s, new TypeReference<List<Setmeal>>() {
            });
            return Result.success(list);
        }
        //如果redis中没有数据就查询mysql数据库
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        list = setmealService.list(queryWrapper);
        //把查询结果序列化后存储到redis中
        stringRedisTemplate.opsForValue().set(key,jacksonObjectMapper.writeValueAsString(list));
        return Result.success(list);
    }
}
