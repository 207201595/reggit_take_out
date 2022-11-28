package com.itheima.controller;

import com.itheima.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/11 - 10 - 11 - 15:24
 */

/**
 * 文件上传和下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     * MultipartFile file 参数名不能随便写 必须和前端的name一致
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file)  {
        /**
         * file是一个临时文件需要转存
         * 如果没有转存本次请求后就会消失了
         */
        log.info(file.toString());
        //获得原始文件名
        String originalFilename = file.getOriginalFilename();

        //使用UUID重新生成文件名(防止重复)
        String subFileName = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString()+subFileName;

        //判断文件夹是否存在（不存在新建）
        File dir = new File(basePath);
        if (!dir.exists()){
            //创建文件夹
            dir.mkdirs();
        }

        //将临时文件转存到指定位置
        try {
            file.transferTo(new File(basePath+fileName));
        }catch (IOException ex){
            ex.printStackTrace();
        }
        return Result.success(fileName);
    }

    @GetMapping("/download")
    public Result<String> download(String name, HttpServletResponse response){
        try {
            //通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            //通过输出流 返回给浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            response.setContentType("image/jpeg");

            byte[] bytes = new byte[1024];
            int len = bufferedInputStream.read(bytes);
            while (len!=-1){
                //这里套流缓冲流所以不用刷新缓冲区
                bufferedOutputStream.write(bytes,0,len);
                len = bufferedInputStream.read(bytes);
            }
            //关闭资源
            bufferedOutputStream.close();
            bufferedInputStream.close();
            outputStream.close();
            fileInputStream.close();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }



}
