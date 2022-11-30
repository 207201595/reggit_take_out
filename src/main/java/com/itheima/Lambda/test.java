package com.itheima.Lambda;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/17 - 10 - 17 - 14:32
 */
public class test {

    public static void main(String[] args) {
        goShow((String name,Integer age) ->{
            System.out.println("name:"+name+"age:"+age);
            return name;
        });
    }

    public static void goShow(Lambda_01 lambda_01){
        lambda_01.show("张三测试拉取",22);
    }


}
