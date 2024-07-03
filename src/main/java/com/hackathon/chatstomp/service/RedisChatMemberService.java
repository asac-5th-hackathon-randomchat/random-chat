package com.hackathon.chatstomp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisChatMemberService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void connectedChatroom(Object chatRoomId, String sessionId, String sender) {
        String key = sender + ":" + sessionId;
        log.info(key);
        String value = "chatRoom:" + chatRoomId;
        this.redisTemplate.opsForValue().set(key, value);
    }

    public void deleteChatMember(String sessionId, String sender) {
        String key = sender + ":" + sessionId;
        log.info("삭제 : " + key);
        this.redisTemplate.delete(key);
    }
}
