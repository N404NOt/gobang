package com.example.gobang.api;

import com.example.gobang.model.User;
import com.example.gobang.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
public class UserAPI {

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    @ResponseBody
    public Object login(String username, String password, HttpServletRequest req) {
        // 关键操作，根据username，去数据库中进行查询
        // 如果匹配到且密码一样就对了
        User user = userMapper.selectByName(username);
        if (user == null || !user.getPassword().equals(password)) {
            // 登录失败
            System.out.println("[login] user=" +username);
            System.out.println("登录失败!");
            return new User();
        }
        // 创建会话对象，方便用户操作
        HttpSession session = req.getSession(true);
        session.setAttribute("user",user);
        return user;
    }

    @PostMapping("/register")
    @ResponseBody
    public Object register(String username,String password) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            userMapper.insert(user);
            return user;
        } catch (org.springframework.dao.DuplicateKeyException e) {
            User user = new User();
            return user;
        }

    }

    @GetMapping("/userInfo")
    @ResponseBody
    public Object getUserInfo(HttpServletRequest request) { // 可以不从数据库获取，因为session存储了当前用户信息
        try {
            HttpSession session = request.getSession(false);
            User user = (User) session.getAttribute("user");  // getAttribute 是Object类型所以强转型
            System.out.println(request);
            return user;
        }catch (NullPointerException e) {
            return new User();
        }
    }
}
