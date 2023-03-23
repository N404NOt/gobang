package com.example.gobang.game;

import com.example.gobang.JavaGobangApplication;
import com.example.gobang.mapper.UserMapper;
import com.example.gobang.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.UUID;

// 这个类表示一个游戏房间

public class Room {
    // 使用字符串类型表示，方便生成唯一值
    private String roomId;

    private User user1;
    private User user2;

    private static final int MAX_ROW = 15;
    private static final int MAX_COL = 15;

    // 先手方的玩家 id
    private int whiteUser;

    // 这个二维数组用来表示棋盘
    // 约定：
    // 1) 使用 0 表示未落子
    // 2) 使用 1 表示玩家一
    // 3) 使用 2 表示玩家二
    private int[][] board = new int[15][15];

//    @Autowired
    OnlineUserManager onlineUserManager;

//    @Autowired
    private RoomManager roomManager;

    private UserMapper userMapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    // 通过这个方法来处理一次落子操作
    // 要做的事情:
    // 1.记录当前落子的位置
    // 2.进行胜负判定
    // 3.给客户端返回响应
    public void putChess(String jsonString) throws IOException {
        // 1.记录当前落子的位置
        GameRequest request = objectMapper.readValue(jsonString,GameRequest.class);
        GameResponse response = new GameResponse();
        // 当前这个子是玩家1 还是玩家2，决定往数组中写入1还是2
        int chess = request.getUserId() == user1.getUserId() ? 1 : 2;
        int row = request.getRow();
        int col = request.getCol();
        if (board[row][col] != 0) {
            // 在客户端已经进行重复落子判断了，为了程序的健壮性在服务器再次判断
            System.out.println("当前位置 (" + row + ", " + col + ") 已经有子了");
            return;
        }
        board[row][col] = chess;
        // 2. 打印出当前的棋盘信息，方便来观察局势，也方便后面验证胜负关系的判断
        printBoard();
        // 3.进行胜负判定
        int winner = checkWinner(row,col,chess);
        // 4.给房间中的所有客户端返回响应
        response.setMessage("putChess");
        response.setUserId(request.getUserId());
        response.setRow(row);
        response.setCol(col);
        response.setWinner(winner);

        // 想要给用户发送 websocket 数据，就需要获取到这个用户的 WebSocketSession
        WebSocketSession session1 = onlineUserManager.getFromGameRoom(user1.getUserId());
        WebSocketSession session2 = onlineUserManager.getFromGameRoom(user2.getUserId());
        // 万一当前查到的会话为空（玩家下线了） 特殊处理
        if (session1 == null) {
            response.setWinner(user2.getUserId());
            System.out.println("玩家1 掉线了！");
        }
        if (session2 == null) {
            response.setWinner(user1.getUserId());
            System.out.println("玩家2 掉线了！");
        }
        // 把响应构造成 JSON 字符串，通过 session 进行传输
        String respJson = objectMapper.writeValueAsString(response);
        if (session1 != null) {
            session1.sendMessage(new TextMessage(respJson));
        }
        if (session2 != null) {
            session2.sendMessage(new TextMessage(respJson));
        }
        // 4.如果当前胜负已分，此时房间就失去了存在的意义了，就可以直接销毁房间（把房间从对了中删除）
        if(response.getWinner() != 0) {
            // 胜负已分
            System.out.println("游戏结束！房间即将销毁！ roomId= " + roomId +"获胜方为：" + response.getWinner());
            // 更新获胜方和失败方的信息
            int winUserId = response.getWinner();
            int loseUserId = response.getWinner() == user1.getUserId() ? user2.getUserId() : user1.getUserId();
            userMapper.userWin(winUserId);
            userMapper.userLose(loseUserId);
            // 销毁房间
            roomManager.remove(roomId,user1.getUserId(),user2.getUserId());
        }

    }

    private void printBoard() {
        // 打印出棋盘
        System.out.println("[打印棋盘信息] " + roomId);
        System.out.println("=================================================");
        for (int r = 0; r < MAX_ROW; r++) {
            for (int c = 0; c < MAX_COL; c++) {
                // 针对一行之内的若干行，不要打印换行
                System.out.printf(board[r][c] + " ");
            }
            System.out.println();
        }
        System.out.println("=================================================");

    }

    // 判断是否落子分出胜负
    // 如果玩家一胜利返回玩家一的userid
    // 如果玩家二赢了返回玩家二的userid
    // 如果胜负未分，就返回0
    private int checkWinner(int row, int col,int chess) {
        // 检查所有的行
        // 先遍历五种情况
        for (int c = col - 4;c<= col;c++) {
            // 针对其中的一种情况，来判定这五个子是不是连接在一起了
            // 不光是这五个子得连起来，而且还得和玩家落的子是一样
            try {
                if (board[row][c] == chess
                        && board[row][c + 1] == chess
                        && board[row][c + 2] == chess
                        && board[row][c + 3] == chess
                        && board[row][c + 4] == chess){
                    // 构成了五子连珠! 胜负已分!
                    return chess == 1 ? user1.getUserId() : user2.getUserId();

                }
            } catch (ArrayIndexOutOfBoundsException e) {
                // 如果出现数组下标越界的情况，就在这里直接忽略这个异常
                continue;
            }


        }

        // 2. 检查所有列
        for (int r = row - 4; r <= row; r++) {
            try {
                if (board[r][col] == chess
                        && board[r + 1][col] == chess
                        && board[r + 2][col] == chess
                        && board[r + 3][col] == chess
                        && board[r + 4][col] == chess) {
                    return chess == 1 ? user1.getUserId() : user2.getUserId();
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }
        }

        // 3. 检查左对角线
        for (int r = row - 4, c = col - 4; r <= row && c <= col; r++, c++) {
            try {
                if (board[r][c] == chess
                        && board[r + 1][c + 1] == chess
                        && board[r + 2][c + 2] == chess
                        && board[r + 3][c + 3] == chess
                        && board[r + 4][c + 4] == chess) {
                    return chess == 1 ? user1.getUserId() : user2.getUserId();
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }
        }

        // 4. 检查右对角线
        for (int r = row - 4, c = col + 4; r <= row && c >= col; r++, c--) {
            try {
                if (board[r][c] == chess
                        && board[r + 1][c - 1] == chess
                        && board[r + 2][c - 2] == chess
                        && board[r + 3][c - 3] == chess
                        && board[r + 4][c - 4] == chess) {
                    return chess == 1 ? user1.getUserId() : user2.getUserId();
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }
        }

        // 胜负未分，直接返回 0 了
        return 0;
    }

    public int getWhiteUser() {
        return whiteUser;
    }

    public void setWhiteUser(int whiteUser) {
        this.whiteUser = whiteUser;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }

    public Room() {
        // 构成 Room 的时候生成一个唯一的字符串表示房间 id
        // 使用UUID 来作为房间 id
        roomId = UUID.randomUUID().toString();

        // 通过入口类中的记录的 context 来手动获取到前面的 RoomManager 和 OnlineUserManager
        // 手动注入, 当前的 room 类应该是多例的所有不能用注入的的方式，想要引入单例的SpringBoot的实例只能通过手动注入的方式
        onlineUserManager = JavaGobangApplication.context.getBean(OnlineUserManager.class);
        roomManager = JavaGobangApplication.context.getBean(RoomManager.class);
        userMapper = JavaGobangApplication.context.getBean(UserMapper.class);
    }
}
