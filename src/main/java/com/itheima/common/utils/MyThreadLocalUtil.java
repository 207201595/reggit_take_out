package com.itheima.common.utils;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/8 - 10 - 08 - 00:48
 */
public class MyThreadLocalUtil {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 把session的id存放到线程副本中
     * @param id
     */
    public static void setId(Long id){
        threadLocal.set(id);
    }

    /**
     * 把session的id取出
     * @return
     */
    public static Long getId(){
        return threadLocal.get();
    }
}
