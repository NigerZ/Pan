package com.ohh.fileServer.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Random;

public class MD5Util {

    /**
     * 随机获取盐值
     * @return
     */
    public static String getRandomSalt() {
        char[] chars = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz" +
                "1234567890!@#$%^&*()_+").toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            //Random().nextInt()返回值为[0,n)
            char aChar = chars[new Random().nextInt(chars.length)];
            sb.append(aChar);
        }
        return sb.toString();
    }


    /**
     *
     * @param originString 待加密字符串
     * @return 加密后的字符串
     */
    public static String toMD5(String originString){
        String rs = null;
        if (originString != null){
            try {
                //创建具有指定算法名称的MessageDigest对象
                MessageDigest md = MessageDigest.getInstance("MD5");
                //使用指定字节数组对对象进行最后更新，完成计算
                byte[] secretByte = md.digest(originString.getBytes());
                rs = byteArrayToString(secretByte);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return rs;
    }

    /**
     * 将字节数组转为16进制字符串
     * @param  secretByte 字节数组
     * @return 字符串
     */
    private static String byteArrayToString(byte[] secretByte){
        //将得到的字节数组变成字符串返回
        String md5Code = new BigInteger(1, secretByte).toString(16);
        StringBuilder code = new StringBuilder(md5Code);

        for (int i = 0; i < 32-code.length() ; i++){
            code.insert(0,"0");
        }
        return code.toString();
    }
}
