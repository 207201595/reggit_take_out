package com.itheima.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.pojo.Employee;
import org.springframework.stereotype.Service;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/5 - 10 - 05 - 00:43
 */
@Service
public interface EmployeeService extends IService<Employee> {


    Page<Employee> page(Integer currentPage,Integer pageSize,String name);


}
