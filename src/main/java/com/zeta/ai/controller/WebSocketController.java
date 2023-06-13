package com.zeta.ai.controller;

import com.zeta.ai.utils.WebSocketUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.UUID;

import static com.zeta.ai.utils.WebSocketUtil.ONLINE_SESSION;

/**
 * 
 * @Description: 向app端实时推送业务状态信息
 * @Date: 2019/7/16
 **/
//由于是websocket 所以原本是@RestController的http形式
//直接替换成@ServerEndpoint即可，作用是一样的 就是指定一个地址
//表示定义一个websocket的Server端
@Component
@ServerEndpoint(value = "/websocket")
public class WebSocketController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketController.class);

    /**
     * 
     * @Description: 加入连接
     * @Return:
     **/
    @OnOpen
    public void onOpen(Session session) {
        String userNo = UUID.randomUUID().toString();
        LOGGER.info("[" + userNo + "]加入连接!");
        WebSocketUtil.addSession(userNo, session);
    }

    /**
     * 
     * @Description: 断开连接
     * @Param session:
     * @Return:
     **/
    @OnClose
    public void onClose(Session session) {
        ONLINE_SESSION.forEach((key, sess) -> {
            if (sess.getId().equals(session.getId())) {
                WebSocketUtil.remoteSession(key);
                LOGGER.info("{} 断开连接", key);
            }
        });
    }

    /**
     * 
     * @Description: 发送消息
     * @Param message: 消息
     * @Return:
     **/
    @OnMessage
    public void onMessage(@PathParam("userNo") String userNo, String message, Session session) {
        if(userNo != null) {
            String messageInfo = "服务器对[" + userNo + "]发送消息：" + message;
            LOGGER.info(messageInfo);
            Session recieverSession = ONLINE_SESSION.get(userNo);
            if ("heart".equalsIgnoreCase(message)) {
                LOGGER.info("客户端向服务端发送心跳");
                //向客户端发送心跳连接成功
                message = "success";
            }
            //发送普通信息
            WebSocketUtil.sendMessage(recieverSession, message);
        } else {
            String _msg = message;
            ONLINE_SESSION.forEach((key, sess) -> {
                if (!sess.getId().equals(session.getId())) {
                    WebSocketUtil.sendMessage(sess, _msg);
                }
            });
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.error(session.getId() + "异常:", throwable);
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        throwable.printStackTrace();
    }
}


