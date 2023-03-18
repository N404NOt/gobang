package com.example.gobang.game;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;

@Component
public class OnlineUserManager {
    // 这个哈希表就是用来表示当前用户在游戏大厅的在线状态
    private HashMap<Integer, WebSocketSession> gameHall = new HashMap<>();

    public void enteryGameHall(int userId,WebSocketSession webSocketSession) {
        gameHall.put(userId,webSocketSession);
    }

    public void exitGameHall(int userId) {
        gameHall.remove(userId);
    }

    public WebSocketSession getFromGameHall(int userId) {
        return gameHall.get(userId);
    }
}
