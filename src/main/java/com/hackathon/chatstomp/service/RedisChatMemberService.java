package com.hackathon.chatstomp.service;

import com.hackathon.chatstomp.dto.ChatRandomResult;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisChatMemberService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void connectedChatroom(Object chatRoomId, String sessionId, String sender) {
        String key = sessionId + ":" + sender;
        log.info("채팅 서버로 던져줌");
        String value = "chatRoom:" + chatRoomId;
        this.redisTemplate.opsForValue().set(key, value);
    }

    public String connectedWaitQueue( String sender) {
        redisTemplate.opsForValue().set("wait:" + sender, sender);
        return "wait:" + sender;
    }

    public List<String> getQueueList() {
        return redisTemplate.keys("*").stream().map((temp)-> temp.split(":")[1]).collect(Collectors.toList());
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

        @Transactional
    public synchronized ChatRandomResult matching(String sender) {
        if (doesKeyExist("wait:"+sender)) {
        List<String> list = getQueueList();

            // 이미 자신과 채팅하고 있는 파트너 리스트에서 삭제
//            List<Long> memberIds = chatRoomService.findMemberChatRoom(groupId, memberId);
//            redisChatQueues = redisChatQueues.stream()
//                    .filter(member -> !memberIds.contains(member.getMemberId()))
//                    .toList();

            // 리스트에 혼자면 return
            if (list.size() == 1) {
                return ChatRandomResult.of(0L, "null");
            }

            Random random = new Random();
            int randomIndex = random.nextInt(list.size());
            String partner = (String) redisTemplate.opsForValue().get("wait:"+list.get(randomIndex));
        System.out.println("dddd   "+partner  );
            while (partner.equals(sender)) {
                randomIndex = random.nextInt(list.size());
//                partner = redisTemplate.keys("wait:"+list.get(randomIndex)).toString();
                partner = (String) redisTemplate.opsForValue().get("wait:"+list.get(randomIndex));
            }
            removeQueue("wait:"+sender);
            removeQueue("wait:"+partner);
        String prefix = "wait:";
        Long chatRoomId = new Random().nextLong();

            return ChatRandomResult.of(chatRoomId, partner);
        } else {
            return ChatRandomResult.of(0L, "null");
        }

    }

    public boolean doesKeyExist(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        return keys != null && !keys.isEmpty();
    }

    public void removeQueue(String key) {
        redisTemplate.delete(key);
    }
}
