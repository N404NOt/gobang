package com.example.gobang.api;

import com.example.gobang.game.OnlineUserManager;
import com.example.gobang.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

// 通过这个类来处理匹配功能中的 websocket 请求
@Component
public class MatchAPI extends TextWebSocketHandler {

    // 处理JASON 和 java对象的转换
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 玩家上线，加入到 OnlineUserManager 中

        // 1.先获取到当前用户的身份信息（谁在游戏大厅中，简历的连接）
        // 此处的代码，之所以可以 getAttributes，全靠了在注册WebSocket 的时候，
        // 加上的addInterceptors （new HttpSessionHandshakeInterceptor());
        // 这个逻辑就把HttpSession 中的 Attributes 都给拿到 WebSocketSession 中了
        // 在 Http 登录逻辑中，往 HttpSession 中存了 User 数据： session.setAttribute("user",user);
        // 此时就可以在 WebSocketSession 中把之前 HttpSession 里存的User 对象给拿到了

        // 如果之前用户据没有通过 Http 来进行登录，直接通过 /game_hall.html 来访问游戏大厅
        // 此时就会出现 user 为 null 的情况
        try {
            User user = (User) session.getAttributes().get("user");
            onlineUserManager.enteryGameHall(user.getUserId(),session);
            System.out.println("玩家" + user.getUsername() + " 进入游戏大厅！");
        } catch (NullPointerException e) {
            e.printStackTrace();
            // 出现空指针异常，说明当前用户的身份信息是空，用户未登录
            // 把当前用户尚未登录这个信息给返回回去
            session.sendMessage();
        }

        // 2.拿到了身份信息后，就可以把玩家设置成在线状态了
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // 玩家下线，从 OnlineUserManager 中删除
        User user = (User) session.getAttributes().get("user");
        onlineUserManager.exitGameHall(user.getUserId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 玩家下线，从 OnlineUserManager 中删除
        User user = (User)session.getAttributes().get("user");
        onlineUserManager.exitGameHall(user.getUserId());

    }
}
