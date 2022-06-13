package com.ohh.fileServer.utils;

import org.springframework.util.ClassUtils;

import java.io.File;

public class FileUtil {

    public static String getFilePath(){
        //获取当前工程的路径
        String path = FileUtil.class.getClassLoader().getResource("").getPath();
        //上传文件的存储路劲
        path = path + "resources/file";
        //获取文件
        File file = new File(path);
        //判断文件是否存在
        if(!file.exists()){
            //不存在创建文件
            file.mkdir();
        }
        return path;
    }
}
