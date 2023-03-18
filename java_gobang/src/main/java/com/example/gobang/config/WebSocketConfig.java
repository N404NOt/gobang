package com.example.gobang.config;

import com.example.gobang.api.MatchAPI;
import com.example.gobang.api.TestAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import javax.annotation.Resource;

@Configuration
@EnableWebSocket  //告诉框架这个是一个websocket注册的类，启用webSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private TestAPI testAPI;

    @Autowired
    private MatchAPI matchAPI;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(new TestAPI(),"test"); //注册添加websocket处理器，然后使用TestAPI中的方法
        registry.addHandler(testAPI,"/test"); //Spring写法,//test是路径，和这个关联起来
        registry.addHandler(matchAPI,"/findMatch").addInterceptors
                (new HttpSessionHandshakeInterceptor());
    }

}

