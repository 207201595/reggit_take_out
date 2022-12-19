package com.itheima.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.itheima.common.Result;

import com.itheima.common.utils.SMSUtils;
import com.itheima.common.utils.ValidateCodeUtils;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: zhangTZ
 * @Date: 2022/11/17 - 11 - 17 - 15:41
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送手机验证码
     * @param user
     * @return
     */
    //json格式的数据需要@RequestBody来接受参数
    @PostMapping("/sendMsg")
    public Result<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();
        System.out.println("手机号"+phone);
        if (StringUtils.isNotEmpty(phone)){
            /**
             * 工具类生成4位生成随机的验证码
             */
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("短息验证码为:"+code);

            //调用阿里云的短信服务API发送短信
//            SMSUtils.sendMessage("阿里云短信测试","SMS_154950909",phone,code);
            //将生成的短信验证码保存到session
//            session.setAttribute(phone,code);
            //将生成的短信验证码保存到redis中 并设置有效期五分钟
            stringRedisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            return Result.success("手机验证码发送成功");
        }


        return Result.error("手机验证码发送失败");
    }
    /**
     * 移动端用户登录验证
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody Map map,HttpSession session){
        log.info(map.toString());
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //从session中获取保存的验证码
//        Object SessionCode = session.getAttribute(phone).toString();
        //从redis中获取保存的验证码
        Object redisCode = stringRedisTemplate.opsForValue().get(phone);
        //进行验证码的比对（页面中提交的验证码和保存的验证码比对）
        if (redisCode!=null && code.equals(redisCode)){
            //登录成功
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if (user==null){
                //登录成功后判断当前的手机号是否为新用户  如果是新用户自动完成注册
                user = new User();
                user.setPhone(phone);
                //默认是禁用 设置一下状态
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            //如果用户登录成功 删除redis的验证码
            stringRedisTemplate.delete(phone);
            return Result.success(user);
        }

        return Result.error("登录失败");
    }

}
