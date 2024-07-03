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
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        log.info(accessor.toString());

        handleMessage(command, accessor);
        return message;
    }

    private void handleMessage(StompCommand command, StompHeaderAccessor accessor) {
        switch (command) {
            case CONNECT:
                log.info("CONNECTED");
                sender = accessor.getFirstNativeHeader("sender");
                chatRoomId = accessor.getFirstNativeHeader("chatRoomId");
                break;
            case SUBSCRIBE:
                log.info("SUBSCRIBED");
                if (accessor.getDestination().startsWith("/waitqueue")) {
                    redisChatMemberService.connectedWaitQueue(accessor.getSessionId(), sender);
                } else {
                    connectToChatRoom(accessor);
                }
                break;
            case DISCONNECT:
                log.info("DISCONNECTED");
                String sessionId = accessor.getSessionId();
                redisChatMemberService.deleteChatMember(sessionId);
                break;
            default:
                break;
        }
    }

    private void connectToChatRoom(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        log.info("connectedToChatRoom , ChatRoomId : " + chatRoomId);
        redisChatMemberService.connectedChatroom(chatRoomId, sessionId, sender);
    }
}
