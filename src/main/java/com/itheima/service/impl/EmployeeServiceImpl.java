package com.itheima.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mapper.EmployeeMapper;
import com.itheima.pojo.Employee;
import com.itheima.service.EmployeeService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/5 - 10 - 05 - 00:45
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper,Employee> implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;


    /**
     * 分页方法
     * @param currentPage
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    public Page<Employee> page(Integer currentPage, Integer pageSize, String name) {

        Page<Employee> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<Employee> employeeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //包含name的条件
        employeeLambdaQueryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //根据更新时间排序
        employeeLambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);
        //查询
        Page<Employee> employeePage = employeeMapper.selectPage(page, employeeLambdaQueryWrapper);

        return employeePage;
    }


}
