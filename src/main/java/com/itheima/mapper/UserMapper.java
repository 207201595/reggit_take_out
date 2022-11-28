package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Auther: zhangTZ
 * @Date: 2022/11/17 - 11 - 17 - 15:39
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
