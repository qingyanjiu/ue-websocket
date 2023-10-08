package com.zeta.ai.controller;

import com.zeta.ai.utils.WebSocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.zeta.ai.utils.WebSocketUtil.ONLINE_SESSION;

@Slf4j
@RestController
@RequestMapping("/socket")
public class UeController {

    @Autowired
    private WebSocketController webSocketController;

    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping("/keepalive")
    public ResponseEntity keepalive() {
        Map map = new HashMap();
        map.put("success", true);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

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
