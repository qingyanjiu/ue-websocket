package com.zeta.ai.controller;

import com.zeta.ai.utils.WebSocketUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.zeta.ai.utils.WebSocketUtil.ONLINE_SESSION;

@RestController
@RequestMapping("/socket")
public class UeController {

    @Autowired
    private WebSocketController webSocketController;

    @RequestMapping("/sendMsg")
    public ResponseEntity sendMsg(String userNo, String message) {
        if (userNo == null) {
            ONLINE_SESSION.forEach((key, session) -> {
                if (!key.equals(userNo)) {
                    WebSocketUtil.sendMessage(session, message);
                }
            });
        } else {
            WebSocketUtil.sendMessage(ONLINE_SESSION.get(userNo), message);
        }
        return ResponseEntity.ok().build();
    }
}
