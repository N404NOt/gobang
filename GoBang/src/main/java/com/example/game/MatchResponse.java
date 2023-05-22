package com.example.gobang.game;

// 这是表示一个 websocket 的匹配响应
public class MatchResponse {
    private boolean ok = true;
    private String reason = "";
    private String message = "";

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}