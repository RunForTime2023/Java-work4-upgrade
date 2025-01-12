package org.webapp.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.webapp.pojo.MessageDO;
import org.webapp.pojo.UserVO;
import org.webapp.pojo.VideoDO;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public boolean isKeyExist(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public void saveUser(UserVO user) {
        stringRedisTemplate.opsForHash().putAll("user:" + user.getUserId(), CustomizeUtils.convertPojoToMap(user));
        stringRedisTemplate.expire("user:" + user.getUserId(), 6, TimeUnit.HOURS);
    }

    public UserVO getUser(String userId) {
        return CustomizeUtils.convertMapToPojo(stringRedisTemplate.opsForHash().entries("user:" + userId), UserVO.class);
    }

    public void updateUser(String userId, String column, Object value) {
        stringRedisTemplate.opsForHash().put("user:" + userId, column, value);
    }

    public void removeUser(String userId) {
        stringRedisTemplate.delete("user:" + userId);
    }

    public void saveVideo(VideoDO video) {
        stringRedisTemplate.opsForHash().putAll("video:" + video.getVideoId(), CustomizeUtils.convertPojoToMap(video));
        stringRedisTemplate.expire("video:" + video.getVideoId(), 6, TimeUnit.HOURS);
        stringRedisTemplate.opsForZSet().add("rank_list", video.getVideoId(), video.getVisitCount());
    }

    public void saveSearchRecord(String userId, String keywords) {
        stringRedisTemplate.opsForList().leftPush("keywords:" + userId, keywords);
        stringRedisTemplate.opsForList().trim("keywords:" + userId, 0, 29);
    }

    public VideoDO getVideo(String videoId) {
        return CustomizeUtils.convertMapToPojo(stringRedisTemplate.opsForHash().entries("video:" + videoId), VideoDO.class);
    }

    public Set<String> listVideoId(long start, long end) {
        return stringRedisTemplate.opsForZSet().range("rank_list", start, end);
    }

    public Long countVideo() {
        return stringRedisTemplate.opsForZSet().size("rank_list");
    }

    public void updateVideo(String column, String videoId, int plus) {
        stringRedisTemplate.opsForHash().increment("video:" + videoId, column, plus);
    }

    public void removeVideo(String videoId) {
        stringRedisTemplate.delete("video:" + videoId);
        stringRedisTemplate.opsForZSet().remove("rank_list", videoId);
    }

    public void saveMessage(MessageDO message) {
        stringRedisTemplate.opsForHash().putAll("message:" + message.getMessageId(), CustomizeUtils.convertPojoToMap(message));
        stringRedisTemplate.expire("message:" + message.getMessageId(), 3, TimeUnit.HOURS);
    }

    public void saveUnreadMessage(String fromUserOrGroupId, String toUserId, String messageId, boolean isGroup) {
        if (isGroup) {
            stringRedisTemplate.opsForList().leftPush("unread:" + toUserId + ":group:" + fromUserOrGroupId, messageId);
        } else {
            stringRedisTemplate.opsForList().leftPush("unread:" + toUserId + ":person:" + fromUserOrGroupId, messageId);
        }
    }

    public MessageDO getMessage(String messageId) {
        return CustomizeUtils.convertMapToPojo(stringRedisTemplate.opsForHash().entries("message:" + messageId), MessageDO.class);
    }

    public List<String> listUnreadUserMessageId(String ownUserId, String otherUserId) {
        return stringRedisTemplate.opsForList().range("unread:" + ownUserId + ":person:" + otherUserId, 0, -1);
    }

    public List<String> listUnreadGroupMessageId(String userId, String groupId) {
        return stringRedisTemplate.opsForList().range("unread:" + userId + ":group:" + groupId, 0, -1);
    }

    public void removeUnreadUserMessageRecord(String ownUserId, String otherUserId) {
        stringRedisTemplate.delete("unread:" + ownUserId + ":person:" + otherUserId);
    }

    public void removeUnreadGroupMessageRecord(String userId, String groupId) {
        stringRedisTemplate.delete("unread:" + userId + ":group:" + groupId);
    }
}
