package com.ohh.fileServer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ohh.fileServer.entity.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    Boolean addUser(User user);
    User findUserByAccount(String account);
}
