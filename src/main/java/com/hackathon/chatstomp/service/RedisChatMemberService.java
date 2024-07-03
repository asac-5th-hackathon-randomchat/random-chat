package com.hackathon.chatstomp.service;

import com.hackathon.chatstomp.dto.ChatRandomResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisChatMemberService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void connectedChatroom(Object chatRoomId, String sessionId, String sender) {
        String key = sessionId + ":" + sender;
        String value = "chatRoom:" + chatRoomId;
        redisTemplate.opsForValue().set(key, value);
    }

    public String connectedWaitQueue(String sessionId, String sender) {
        redisTemplate.opsForValue().set("wait:" + sender, sender);
        return "wait:" + sender;
    }

    public List<String> getQueueList() {
        Set<String> keys = redisTemplate.keys("wait:*");
        if (keys == null) {
            return List.of();
        }
        return keys.stream().map(key -> key.split(":")[1]).collect(Collectors.toList());
    }

    public void deleteChatMember(String sessionId) {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null) {
            for (String key : keys) {
                if (key.startsWith(sessionId + ":")) {
                    redisTemplate.delete(key);
                }
            }
        }
    }

    public ChatRandomResult matching(String sender) {
        List<String> redisChatQueues = getQueueList();

        if (redisChatQueues.size() <= 1) {
            return ChatRandomResult.of(0L, null);
        }

        Random random = new Random();
        String partner;
        do {
            partner = redisChatQueues.get(random.nextInt(redisChatQueues.size()));
        } while (partner.equals(sender));

        removeQueue("wait:" + sender);
        removeQueue("wait:" + partner);

        Long chatRoomId = generateChatRoomId(); //

        return ChatRandomResult.of(chatRoomId, partner);


        // Implement this method to generate a unique chat room ID
    }

    public void removeQueue(String key) {
        redisTemplate.delete(key);
    }

    private Long generateChatRoomId() {
        // Implement your logic to generate a unique chat room ID
        return new Random().nextLong();
    }

}
