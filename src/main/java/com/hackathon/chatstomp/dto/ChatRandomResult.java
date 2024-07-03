package com.hackathon.chatstomp.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ChatRandomResult {

    private final Long chatRoomId;
    private final String partner;

    @Builder
    private ChatRandomResult(Long chatRoomId, String partner) {
        this.chatRoomId = chatRoomId;
        this.partner = partner;
    }

    public static ChatRandomResult of(Long chatRoomId, String partner) {
        return ChatRandomResult.builder()
                .chatRoomId(chatRoomId)
                .partner(partner)
                .build();
    }
}
