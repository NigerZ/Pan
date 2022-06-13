package com.ohh.fileServer.service.impl;

import com.ohh.fileServer.entity.User;
import com.ohh.fileServer.mapper.UserMapper;
import com.ohh.fileServer.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.rmi.ServerException;

@Service
@SuppressWarnings("all")
public class UserService implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public Boolean addUser(User user) throws ServerException {
        User result = userMapper.findUserByAccount(user.getAccount());
        if(result == null){
            user.setName(user.getAccount());
            user.setIsDeleted(0);
            return userMapper.addUser(user);
        }
        throw new ServerException("账号已存在");
    }
}
