package com.zeta.ai.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeta.ai.utils.WebSocketUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.zeta.ai.utils.WebSocketUtil.ONLINE_SESSION;
import static com.zeta.ai.utils.WebSocketUtil.MEETING_SESSION_MAPPER;

/**
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

    private static final String EVENT_ONLINE_WITH_VIDEO_AUDIO_STATE = "ONLINE_WITH_VIDEO_AUDIO_STATE_EVENT";
    private static final String EVENT_VIDEO_AUDIO_STATE = "VIDEO_AUDIO_STATE_EVENT";
    private static final String EVENT_ONLINE = "ONLINE_EVENT";
    private static final String EVENT_OFFLINE = "OFFLINE_EVENT";
    private static final String EVENT_START_SHARE = "START_SHARE_EVENT";
    private static final String EVENT_STOP_SHARE = "STOP_SHARE_EVENT";

    private ObjectMapper objectMapper = new ObjectMapper();


    /**
     * @Description: 加入连接
     * @Return:
     **/
    @OnOpen
    public String onOpen(Session session) {
        String userNo = UUID.randomUUID().toString();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[" + userNo + "]加入连接!");
        }
        WebSocketUtil.addSession(userNo, session);
        return userNo;
    }

    /**
     * @Description: 断开连接
     * @Param session:
     * @Return:
     **/
    @OnClose
    public void onClose(Session session) {
        ONLINE_SESSION.forEach((key, sess) -> {
            if (sess.getId().equals(session.getId())) {
                WebSocketUtil.remoteSession(key);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("{} 断开连接", key);
                }
            }
        });
    }

    /**
     * @Description: 发送消息
     * @Param message: 消息
     * @Return:
     **/
    @OnMessage
    public void onMessage(@PathParam("userNo") String userNo, String message, Session session) {
        if (userNo != null) {
            String messageInfo = "服务器对[" + userNo + "]发送消息：" + message;
            LOGGER.info(messageInfo);
            Session recieverSession = ONLINE_SESSION.get(userNo);
            //发送普通信息
            WebSocketUtil.sendMessage(recieverSession, message);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("接收到事件信息{}", message);
            }
            // 接收到客户端心跳
            if ("keepalive".equalsIgnoreCase(message)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("客户端 {} 向服务端发送心跳", session.getId());
                }
                //向客户端发送心跳连接成功
                WebSocketUtil.sendMessage(session, "success");
            } else {
                Map<String, Object> data = null;
                try {
                    data = objectMapper.readValue(message, Map.class);
                } catch (JsonProcessingException e) {
                    LOGGER.error(e.getMessage());
                }

                // 事件名称
                String eventName = data.get("eventName").toString();
                Map<String, String> payload = (Map<String, String>) data.get("payload");
                // 会议id
                String meetingId = payload.get("meetingId");
                // userId
                String userId = payload.get("userId");
                if (StringUtils.isNotBlank(meetingId) && StringUtils.isNotBlank(eventName)) {
                    // 视频上线事件，保存会议信息到session
                    if (EVENT_ONLINE_WITH_VIDEO_AUDIO_STATE.equals(eventName) || EVENT_ONLINE.equals(eventName)) {
                        // 获取会议中session列表
                        Map<String, Session> map = MEETING_SESSION_MAPPER.getOrDefault(meetingId, new HashMap<>());
                        // 新session加入
                        map.put(userId, session);
                        // 加入mapper，在判断用户和会议室的时候可以用
                        MEETING_SESSION_MAPPER.put(meetingId, map);
                        // @@@@@@@@@@@@ 可以考虑加个定时判断空map给删除的逻辑，每天夜里轮训一次，把所有结束的会议信息删掉
                    } else if (EVENT_OFFLINE.equals(eventName)) {
                        // 视频下线事件，删除相关数据
                        // 获取会议中session列表
                        Map<String, Session> map = MEETING_SESSION_MAPPER.getOrDefault(meetingId, new HashMap<>());
                        map.remove(userId);
                    }
                    // 其他事件类型，直接转发
                    // 发送所有类型的事件广播
                    sendMsgToAttendeeInMeeting(meetingId, userId, session, message);
                }
            }

        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.error(session.getId() + "异常:", throwable);
//        try {
//            session.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        throwable.printStackTrace();
    }

    private void sendMsgToAttendeeInMeeting(String meetingId, String userId, Session mySession, String message) {
        if (MEETING_SESSION_MAPPER.size() > 0) {
            // 需要接收数据的session列表，仅发送给同一个会议室的人, 但不包括自己
            MEETING_SESSION_MAPPER.get(meetingId).forEach((k, v) -> {
                if (!k.equals(userId)) {
                    try {
                        if (v.isOpen()) {
                            WebSocketUtil.sendMessage(v, message);
                        } else {
                            LOGGER.info("session已断开，不发送{}", v.getId());
                        }
                    } catch (Exception e) {
                        LOGGER.warn("发送到客户端{}失败，跳过", v.getId());
                    }
                }
            });
        }
    }
}


