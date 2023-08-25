package com.zeta.ai.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 *
 * @Description:
 * @Date: 2019/7/16
 **/
public class WebSocketUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketUtil.class);

    /**
     * 
     * @Description: 使用map进行存储在线的session
     * @Date: 2019/7/16
     **/
    public static final Map<String, Session> ONLINE_SESSION = new ConcurrentHashMap<>();

    // 存储上线的session都在哪个会议室里面。key: meetingId, value: Map<userId, session>
    public static final Map<String, Map<String, Session>> MEETING_SESSION_MAPPER = new ConcurrentHashMap<>();

    /**
     * 
     * @Description: 添加Session
     * @Date: 2019/7/16
     * @Param userKey:
     * @Param session:
     * @Return:
     **/
    public static void addSession(String userKey, Session session) {
        ONLINE_SESSION.put(userKey, session);
    }

    public static void remoteSession(String userKey) {
        ONLINE_SESSION.remove(userKey);
    }

    /**
     * 
     * @Description: 向某个用户发送消息
     * @Date: 2019/7/16
     * @Param session:
     * @Param message:
     * @Return:
     **/
    public static void sendMessage(Session session, String message) {
        if (session == null) {
            LOGGER.error("发送对象未找到，放弃发送");
            return;
        }
        // getAsyncRemote()和getBasicRemote()异步与同步
        Async async = session.getAsyncRemote();
        //发送消息
        Future<Void> future = async.sendText(message);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("服务器发送给客户端" + session.getId() + "的消息:" + message);
        }
    }
}


