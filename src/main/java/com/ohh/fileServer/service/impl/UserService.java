package com.ohh.fileServer.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ohh.fileServer.entity.User;
import com.ohh.fileServer.mapper.UserMapper;
import com.ohh.fileServer.service.IUserService;
import com.ohh.fileServer.utils.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.rmi.ServerException;
import java.util.Date;

/**
 * 用户业务类
 */
@Service
@SuppressWarnings("all")
public class UserService implements IUserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户添加逻辑
     * @param user
     * @return
     * @throws ServerException
     */
    @Override
    public Boolean addUser(User user) throws ServerException {
        User result = userMapper.findUserByAccount(user.getAccount());
        if(result == null){
            //获取原始密码
            String password = user.getPassword();
            //获取加密盐值
            String salt = MD5Util.getRandomSalt();
            //对密码加密
            String md5Password = MD5Util.toMD5(password + salt);
            user.setPassword(md5Password);
            //设置用户信息
            user.setSalt(salt);
            user.setName(user.getAccount());
            user.setIsDeleted(0);
            Boolean isSuccess = userMapper.addUser(user);
            //创建用户成功，生成redis用户信息
            if (isSuccess) {
                redisTemplate.opsForValue().set(user.getAccount(),md5Password);
            }
            return isSuccess;
        }
        throw new ServerException("账号已存在");
    }

    /**
     * 修改用户信息
     * @param user
     * @return
     */
    @Override
    public Integer updateUser(User user) {
        if (user == null){
            try {
                throw new ServerException("未有修改选项");
            } catch (ServerException e) {
            }
        }
        //获取用户原来的信息
        User result = userMapper.findUserByAccount(user.getAccount());
        //使用mp做修改
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        userUpdateWrapper.eq("account", user.getAccount());
        userUpdateWrapper.set(!StringUtils.isEmpty(user.getPassword()), "password",
                MD5Util.toMD5(user.getPassword() + result.getSalt()));
        userUpdateWrapper.set(!StringUtils.isEmpty(user.getName()), "name", user.getName());
        userUpdateWrapper.set("update_time",new Date());
        int rows = userMapper.update(user, userUpdateWrapper);
        //更新redis的密码
        if(rows > 0 && !StringUtils.isEmpty(user.getPassword())){
            redisTemplate.opsForValue().set(user.getAccount(),MD5Util.toMD5(user.getPassword() + result.getSalt()));
        }
        return rows;
    }

}
