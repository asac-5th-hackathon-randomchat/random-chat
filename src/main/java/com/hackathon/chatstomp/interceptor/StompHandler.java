package com.hackathon.chatstomp.interceptor;

import com.hackathon.chatstomp.service.RedisChatMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class StompHandler implements ChannelInterceptor {
    private final RedisChatMemberService redisChatMemberService;
    private String sender;
    private String chatRoomId;
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // 메시지의 헤더에 접근하기 위해 StompHeadAccessor 를 사용
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        log.info(accessor.toString());

        // 명령 가져오기 => accessor.getCommand()
        handleMessage(command, accessor);
        return message;
    }
    private void handleMessage(StompCommand command, StompHeaderAccessor accessor) {
        // FIXME : parameter로 sender랑 sessionid 받는거 고려
        switch (command) {
            case CONNECT:
                log.info("CONNECTED");
                sender = accessor.getFirstNativeHeader("sender");
                chatRoomId = accessor.getFirstNativeHeader("chatRoomId");
                break;
            case SUBSCRIBE:
                log.info("subscribed");
                break;
            case DISCONNECT:
                log.info("DISCONNECTED");
                String sessionId = accessor.getSessionId();
                //log.info("sender" + sender);
                redisChatMemberService.deleteChatMember(sessionId);
                break;
            // SUBSCRIBE일 때 구현할 로직?
        }
    }

    // 채팅이 연결되었을 때, 채팅방 입장 처리함과 동시에 redis에 입장 내역을 저장할 거임
    private void connectToChatRoom(StompHeaderAccessor accessor) {
        // 채팅방 번호를 가져옴
        //Long chatRoomId = getChatRoomId(accessor);
        String sessionId = accessor.getSessionId();
        //String sender = accessor.getFirstNativeHeader("sender");
        log.info("connectedTochatRoom , ChatRoomId : " + chatRoomId);
        //채팅방 입장 처리 -> Redis에 입장 내역 저장
        redisChatMemberService.connectedChatroom(chatRoomId, sessionId, sender);
    }

    private Long getChatRoomId(StompHeaderAccessor accessor) {
        return Long.valueOf(accessor.getFirstNativeHeader("chatRoomId"));
    }
}
