package com.ohh.fileServer.service.impl;

import com.ohh.fileServer.entity.User;
import com.ohh.fileServer.mapper.IUserMapper;
import com.ohh.fileServer.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerErrorException;

import java.rmi.ServerException;

@Service
@SuppressWarnings("all")
public class UserService implements IUserService {
    @Autowired
    private IUserMapper userMapper;

    @Override
    public Boolean addUser(User user) throws ServerException {
        User result = userMapper.findUserByAccount(user.getAccount());
        if(result == null){
            user.setName(user.getAccount());
            return userMapper.addUser(user);
        }
        throw new ServerException("账号已存在");
    }
}
