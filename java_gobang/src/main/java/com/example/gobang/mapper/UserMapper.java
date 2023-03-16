package com.example.gobang.mapper;

import com.example.gobang.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    // 向数据库插入一个用户，用于注册功能
    void insert(User user);

    // 根据用户名，查询用户详细信息，用于登录功能
    User selectByName(String username);
}
