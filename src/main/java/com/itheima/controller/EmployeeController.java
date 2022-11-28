package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.Result;
import com.itheima.pojo.Employee;
import com.itheima.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/5 - 10 - 05 - 00:47
 */
@RestController
@Slf4j
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public Result<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        /**
         * 将页面提交的密码passmord进行md5加密处理
         * 根据页面提交的用户名username 查询数据
         * 如果没有查询到则返回登录失败结果
         * 密码比对，如果不一致网返回登灵失败结果
         * 查看员工状态，如果为己禁用状态，则返回员工已禁用结果
         * 登灵成功，将员工id存入Session并返回登录成功结果
         */
        //1、将页面提交的密码passmord进行md5加密处理
        String username = employee.getUsername();
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2、根据页面提交的用户名username 查询数据
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,username);
        Employee emp = employeeService.getOne(queryWrapper);
        //3、如果没有查询到用户名直接返回
        if (emp==null){
            return Result.error("用户名不存在");
        }
        //4、密码比对，如果不一致网返回登灵失败结果
        if (!emp.getPassword().equals(password)){
            return Result.error("密码错误");
        }
        //5、查看员工状态，如果为己禁用状态，则返回员工已禁用结果 0是禁用 1是可用
        if (emp.getStatus() == 0){
            return Result.error("账号已经被封禁");
        }
        //6、登灵成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return Result.success(emp);
    }
    /**
     *退出功能
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request){
        HttpSession session = request.getSession();
        session.removeAttribute("employee");
        return Result.success("退出成功");
    }
    /**
     *
     * 新增员工数据
     */
    @PostMapping
    public Result<String> save(@RequestBody Employee employee,HttpServletRequest request){
        log.info("输出的员工信息为 {}",employee);
        //设置初始密码 并且进行MD5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        log.info("controller：线程ID为{}",Thread.currentThread().getId());
        boolean save = employeeService.save(employee);
        if (save = false){
            return Result.error("用户创建成功");
        }
        return Result.success("用户创建失败");
    }
    /**
     * 分页方法
     */
    @GetMapping("/page")
    public Result<Page> page(Integer page,Integer pageSize,Employee employee){
        log.info("数据"+page+pageSize+employee.getName());

        Page<Employee> empPage = employeeService.page(page, pageSize, employee.getName());
        return Result.success(empPage);

    }
    /**
     * 根据员工id修改员工信息
     */
    @PutMapping
    public Result<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());
        boolean flag = employeeService.updateById(employee);
        return Result.success("修改成功");
    }
    /**
     * 点击编辑按钮显示即将修改的数据
     */
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id){

        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }
}
