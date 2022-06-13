package com.ohh.fileServer.controller;

import com.ohh.fileServer.dto.R;
import com.ohh.fileServer.entity.User;
import com.ohh.fileServer.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.rmi.ServerException;

@RestController
@RequestMapping("/user")
@Api("用户controller")
public class UserContoller {
    @Autowired
    private IUserService userService;

    @PostMapping("/addUser")
    @ApiOperation(value = "添加用户接口", notes = "传入用户的账号和密码")
    public R addUser(User user) throws ServerException {
        if(user.getAccount() == null){
            throw new ServerException("用户没有输入账号");
        }
        if(user.getPassword() == null){
            throw new ServerException("用户没有输入密码");
        }
        return R.success(userService.addUser(user));
    }
}
