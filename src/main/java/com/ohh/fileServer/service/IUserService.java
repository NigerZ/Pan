package com.ohh.fileServer.service;

import com.ohh.fileServer.entity.User;

import java.rmi.ServerException;

public interface IUserService {
    Boolean addUser(User user) throws ServerException;
    Integer updateUser(User user);
}
