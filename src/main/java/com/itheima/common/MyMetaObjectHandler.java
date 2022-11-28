package com.itheima.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.itheima.common.utils.MyThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/7 - 10 - 07 - 23:54
 */

/**
 * 自定义的元数据处理器用来填充数据
 */
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("元数据处理器：线程ID为{}",Thread.currentThread().getId());
        /**
         * 设置创建时间 更新时间等信息
         */
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        /**
         * 当前用户的创建人 更新人等信息
         */
        metaObject.setValue("createUser", MyThreadLocalUtil.getId());
        metaObject.setValue("updateUser", MyThreadLocalUtil.getId());

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        /**
         * 当前用户的更新时间 更新人等信息
         */
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", MyThreadLocalUtil.getId());
    }
}
