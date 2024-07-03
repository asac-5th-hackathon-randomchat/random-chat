package com.hackathon.chatstomp.controller;

import com.hackathon.chatstomp.Message;
import com.hackathon.chatstomp.dto.ChatRandomResult;
import com.hackathon.chatstomp.service.RedisChatMemberService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.async.DeferredResult;

@Controller
@RequiredArgsConstructor
public class MessageController {
    private final SimpMessageSendingOperations simpMessageSendingOperations;
    private final RedisChatMemberService redisChatMemberService;

    private final Map<String, DeferredResult<ResponseEntity<Map<String, Object>>>> que = new ConcurrentHashMap<>();
    /*
        /sub/channel/{id}       - 구독(channelId:12345)
        /pub/message            - 메시지 발행 경로
    */
    // 메시지 보내주는 거
    @MessageMapping("/message")
    public void sendMessage(Message message) {
        simpMessageSendingOperations.convertAndSend("/sub/channel/" + message.getChatRoomId(), message);
    }

    @PostMapping("/api/chats/random/{sender}")
    public DeferredResult<ResponseEntity<Map<String, Object>>> startMatching(@PathVariable String sender ) {
        // 매칭 상대 찾지 못하고 타임아웃되면, 이 값이 반환됨
        DeferredResult<ResponseEntity<Map<String, Object>>> deferredResult =
                new DeferredResult<>(10 * 1000L, ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body(Map.of("message", "상대를 찾을 수 없습니다.")));

        // redis 대기열에 저장 & Map 에 저장
        String key = redisChatMemberService.connectedWaitQueue(sender);

        que.put(key.split(":")[1], deferredResult);

        // service 에서 매칭 상대 찾기
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ChatRandomResult randomResult = redisChatMemberService.matching(sender);

        // 본인 deferredResult.setResult
        if (!randomResult.getChatRoomId().equals(0L)||!randomResult.getPartner().equals("null")) {
            deferredResult.setResult(
                    ResponseEntity.status(HttpStatus.CREATED).body(Map.of("chatRoomId", randomResult.getChatRoomId())));

            // 매칭 상대방 deferredResult.setResult
            String partnerKey = randomResult.getPartner();
            DeferredResult<ResponseEntity<Map<String, Object>>> partnerDeferredResult = que.get(partnerKey);
            partnerDeferredResult.setResult(
                    ResponseEntity.status(HttpStatus.CREATED).body(Map.of("chatRoomId", randomResult.getChatRoomId())));
        }
        // redis 대기열 삭제 & Map 에서 삭제
        String partnerKey = "wait:" + randomResult.getPartner();
        deferredResult.onCompletion(() -> {
            redisChatMemberService.removeQueue(key);
            redisChatMemberService.removeQueue(partnerKey);
            que.remove(key);
            que.remove(partnerKey);
        });

        return deferredResult;
    }

    // TODO : 매칭 알고리즘

    // TODO : api list

}

