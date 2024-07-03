//package com.hackathon.chatstomp.entity;
//
//import lombok.Builder;
//import lombok.Getter;
//import org.springframework.data.redis.core.RedisHash;
//import org.springframework.stereotype.Indexed;
//
//import javax.persistence.Id;
//
//@Getter
//@RedisHash(value = "chatMember")
//public class ChatMember {
//    @Id
//    private String id;
//
//    @Indexed
//    private Long chatRoomId;
//
//    @Indexed
//    private String userName;
//
//    @Builder
//    public ChatMember(Long chatRoomId, String userName) {
//        this.chatRoomId = chatRoomId;
//        this.userName = userName;
//    }
//
//}
