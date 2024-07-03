package com.hackathon.chatstomp.controller;

import com.hackathon.chatstomp.Message;
import com.hackathon.chatstomp.dto.ChatRandomResult;
import com.hackathon.chatstomp.service.RedisChatMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MessageController {
    private final RedisTemplate<String, Object> redisTemplate;
    private final Map<String, DeferredResult<ResponseEntity<Map<String, Object>>>> que = new ConcurrentHashMap<>();
    private final SimpMessageSendingOperations simpMessageSendingOperations;
    private final RedisChatMemberService redisChatMemberService;

    @MessageMapping("/message")
    public void sendMessage(Message message) {
        simpMessageSendingOperations.convertAndSend("/sub/channel/" + message.getChatRoomId(), message);
    }

    @PostMapping("/api/chats/random/{sender}")
    public DeferredResult<ResponseEntity<Map<String, Object>>> startMatching(@PathVariable String sender) {
        DeferredResult<ResponseEntity<Map<String, Object>>> deferredResult =
                new DeferredResult<>(20 * 1000L, ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body(Map.of("message", "상대를 찾을 수 없습니다.")));

        // redis 대기열에 저장 & Map 에 저장
        String key = "wait:" + sender;
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        valueOps.set(key, sender);  // Redis에 sender 저장

        que.put(key, deferredResult);

        // service 에서 매칭 상대 찾기
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ChatRandomResult randomResult = redisChatMemberService.matching(sender);

        // 본인 deferredResult.setResult
        if (!randomResult.getChatRoomId().equals(0L)) {
            deferredResult.setResult(
                    ResponseEntity.status(HttpStatus.CREATED).body(Map.of("chatRoomId", randomResult.getChatRoomId())));

            // 매칭 상대방 deferredResult.setResult
            String partnerKey = "wait:" + randomResult.getPartner();
            DeferredResult<ResponseEntity<Map<String, Object>>> partnerDeferredResult = que.get(partnerKey);
            partnerDeferredResult.setResult(
                    ResponseEntity.status(HttpStatus.CREATED).body(Map.of("chatRoomId", randomResult.getChatRoomId())));
        }

        // redis 대기열 삭제 & Map 에서 삭제
        String partnerKey = "wait:" + randomResult.getPartner();
        deferredResult.onCompletion(() -> {
            redisTemplate.delete(key);
            redisTemplate.delete(partnerKey);
            que.remove(key);
            que.remove(partnerKey);
        });

        return deferredResult;
    }



    private Long generateChatRoomId() {
        // Implement your logic to generate a unique chat room ID
        return new Random().nextLong();
    }
}
