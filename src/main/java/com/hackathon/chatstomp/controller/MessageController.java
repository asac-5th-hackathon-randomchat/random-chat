package com.hackathon.chatstomp.controller;

import com.hackathon.chatstomp.Message;
import com.hackathon.chatstomp.dto.ChatSendMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageController {
    private final SimpMessageSendingOperations simpMessageSendingOperations;
    /*
        /sub/channel/{id}       - 구독(channelId:12345)
        /pub/message            - 메시지 발행 경로
    */
    // 메시지 보내주는 거
    @MessageMapping("/message")
    public void sendMessage(Message message) {
        simpMessageSendingOperations.convertAndSend("/sub/channel/" + message.getChatRoomId(), message.getData());
    }



    // TODO : 매칭 알고리즘

    // TODO : api list

}

