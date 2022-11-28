package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pojo.AddressBook;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Auther: zhangTZ
 * @Date: 2022/11/17 - 11 - 17 - 21:51
 */
@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}
