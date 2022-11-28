package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pojo.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/5 - 10 - 05 - 00:43
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
