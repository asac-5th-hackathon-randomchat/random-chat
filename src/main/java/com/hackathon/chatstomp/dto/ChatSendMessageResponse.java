package com.hackathon.chatstomp.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ChatSendMessageResponse {
    private Long chatRoomId;
    private String message;
    private String sender;

    @Builder
    private ChatSendMessageResponse(Long chatRoomId, String message, String sender) {
        this.chatRoomId = chatRoomId;
        this.message = message;
        this.sender = sender;
    }

}
