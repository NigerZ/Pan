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

    @PostMapping("/updateUser")
    @ApiOperation("修改用户信息")
    public R updateUser(User user) throws ServerException {
        Integer rows = userService.updateUser(user);
        if(rows == 0){
            throw new ServerException("出现未知问题");
        }
        return R.success(rows);
    }
    @PostMapping("deleteUser")
    @ApiOperation("删除用户信息")
    public R deleteUser(User user){
        return R.success(userService.deleteUser(user));
    }

    @PostMapping("/login")
    @ApiOperation("登录")
    public R login(User user){
        return R.success(userService.login(user));
    }
}
