package com.itheima.img;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.ArrayList;

/**
 * @Auther: zhangTZ
 * @Date: 2022/11/16 - 11 - 16 - 16:58
 */
@SpringBootTest
public class imgTest {
    @Value("${reggie.path}")
    private String basePath;


    @Test
    public void delete(){
        ArrayList<String> stringArrayList = new ArrayList<>();
        stringArrayList.add(basePath+"11.jpg");
        stringArrayList.add(basePath+"22.jpg");
        long count = stringArrayList.stream().filter(img -> {
            File file = new File(img);
            return file.delete();
        }).count();
        System.out.println("删除了"+count);


    }
}
