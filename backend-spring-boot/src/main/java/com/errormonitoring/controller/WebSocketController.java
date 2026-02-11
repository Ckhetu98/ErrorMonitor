package com.errormonitoring.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/error")
    @SendTo("/topic/errors")
    public String handleError(String message) {
        return message;
    }

    public void sendErrorNotification(String applicationId, String errorMessage) {
        messagingTemplate.convertAndSend("/topic/errors/" + applicationId, errorMessage);
    }
}