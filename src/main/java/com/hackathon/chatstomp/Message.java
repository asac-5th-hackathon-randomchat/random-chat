package com.hackathon.chatstomp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    // 메시지 스펙 정의
    private String sender;
    private String chatRoomId;
    private Object data;

    public void setSender(String sender) { this.sender = sender; }
}
