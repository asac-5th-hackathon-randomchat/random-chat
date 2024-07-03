package com.hackathon.chatstomp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisChatMemberService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void connectedChatroom(Object chatRoomId, String sessionId, String sender) {
        String key = sessionId + ":" + sender;
        log.info(key);
        String value = "chatRoom:" + chatRoomId;
        this.redisTemplate.opsForValue().set(key, value);
    }

    public void deleteChatMember(String sessionId) {
        // 모든 키 검색
        Set<String> keys = redisTemplate.keys("*");

            if (keys != null) {
                for (String key : keys) {
                    // 키에 sessionId가 포함되어 있는지 확인
                    if (key.startsWith(sessionId  + ":")) {
                        log.info("삭제 : " + key);
                        redisTemplate.delete(key);
                    }
                }
            }
        }

    }
