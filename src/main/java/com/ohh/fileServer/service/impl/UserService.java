package com.ohh.fileServer.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ohh.fileServer.entity.User;
import com.ohh.fileServer.mapper.UserMapper;
import com.ohh.fileServer.service.IUserService;
import com.ohh.fileServer.utils.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.rmi.ServerException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

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
//                redisTemplate.opsForValue().set(user.getAccount(),md5Password);
                redisTemplate.opsForHash().put(user.getAccount(),"password", md5Password);
                redisTemplate.opsForHash().put(user.getAccount(),"salt", salt);
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
//            redisTemplate.opsForValue().set(user.getAccount(),MD5Util.toMD5(user.getPassword() + result.getSalt()));
            redisTemplate.opsForHash().put(user.getAccount(), "password", MD5Util.toMD5(user.getPassword() + result.getSalt()));
        }
        return rows;
    }

    /**
     * 删除用户
     * @param user
     * @return
     */
    @Override
    public Integer deleteUser(User user) {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("account", user.getAccount());
        int delete = userMapper.delete(userQueryWrapper);
        if(delete > 0){
//            redisTemplate.opsForValue().getAndDelete(user.getAccount());
            redisTemplate.opsForHash().delete(user.getAccount());
        }
        return delete;
    }

    /**
     * 用户的登录逻辑
     * @param user
     * @return
     */

    @Override
    public Boolean login(User user) {
        //获取用户输入的账号密码
        String account = user.getAccount();
        String password = user.getPassword();
        //从redis中获取用户信息
        Map<Object, Object> resultMap = redisTemplate.opsForHash().entries(account);
        //判断redis是否存在
        if(!resultMap.isEmpty()){
            String mapPassword = resultMap.get("password").toString();
            String salt = resultMap.get("salt").toString();
            if (MD5Util.toMD5(password + salt).equals(mapPassword)) {
                return true;
            }else {
                try {
                    throw new ServerException("密码错误");
                } catch (ServerException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        //redis中没有记录，从数据库中搜索
        User result = userMapper.findUserByAccount(account);
        if (user ==null) {
            try {
                throw new ServerException("用户不存在");
            } catch (ServerException e) {
                throw new RuntimeException(e);
            }
        }
        String salt = result.getSalt();
        String resultPassword = result.getPassword();
        if(MD5Util.toMD5(password + salt).equals(resultPassword)){
            //将用户信息重新存入redis中
            redisTemplate.opsForHash().put(account,"password", resultPassword);
            redisTemplate.opsForHash().put(account,"salt", salt);
            return true;
        }else {
            try {
                throw new ServerException("密码错误");
            } catch (ServerException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
