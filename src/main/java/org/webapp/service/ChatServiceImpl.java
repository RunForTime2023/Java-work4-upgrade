package org.webapp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.webapp.mapper.GroupMapper;
import org.webapp.mapper.MemberMapper;
import org.webapp.mapper.MessageMapper;
import org.webapp.pojo.GroupDO;
import org.webapp.pojo.MemberDO;
import org.webapp.pojo.MessageDO;
import org.webapp.utils.FileUtils;
import org.webapp.utils.RedisUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private RedisUtils redisTools;

    @Override
    public MessageDO saveMessage(String fromUserId, String toUserId, String toGroupId, String content, String image) {
        MessageDO message = new MessageDO(fromUserId, toUserId, toGroupId, content);
        messageMapper.insert(message);
        String imageUrl = "D:\\To be Deleted\\ServerData\\MessageImage\\" + message.getMessageId() + ".jpg";
        if ("none".equals(image) || !FileUtils.saveImage(fromUserId, toUserId, image, imageUrl)) {
            imageUrl = "none";
        }
        messageMapper.updateMessage(message.getMessageId(), imageUrl);
        message.setImageUrl(imageUrl);
        redisTools.saveMessage(message);
        if ("none".equals(toGroupId)) {
            redisTools.saveUnreadMessage(fromUserId, toUserId, message.getMessageId(), false);
            log.info("The user: {} send the message to the user: {}.", fromUserId, toUserId);
        } else {
            List<MemberDO> memberList = memberMapper.getMembersInGroup(fromUserId, toGroupId);
            for (MemberDO member : memberList) {
                redisTools.saveUnreadMessage(toGroupId, member.getFromUserId(), message.getMessageId(), true);
            }
            log.info("The user: {} send the message to the group: {}.", fromUserId, toGroupId);
        }
        return message;
    }

    @Override
    public GroupDO saveGroup(String groupName, String userId, List<String> memberList) {
        LocalDateTime time = LocalDateTime.now();
        GroupDO group = new GroupDO(groupName, userId, memberList.size(), time);
        groupMapper.insert(group);
        for (String memberId : memberList) {
            MemberDO member = new MemberDO(memberId, group.getGroupId(), time);
            memberMapper.insert(member);
        }
        log.info("The user: {} creates the group: {}.", userId, group.getGroupId());
        return group;
    }

    @Override
    public void saveMember(String userId, String groupId) {
        LocalDateTime time = LocalDateTime.now();
        if (memberMapper.getMember(userId, groupId) == null) {
            MemberDO member = new MemberDO(userId, groupId, time);
            memberMapper.insert(member);
        } else {
            memberMapper.updateMember(userId, groupId, time);
        }
        groupMapper.updateMemberCount(groupId, 1, time);
        log.info("The user: {} joins the group: {}.", userId, groupId);
    }

    @Override
    public GroupDO getGroup(String groupId) {
        return groupMapper.getGroup(groupId);
    }

    @Override
    public MemberDO getMember(String userId, String groupId) {
        return memberMapper.getMember(userId, groupId);
    }

    @Override
    public List<MessageDO> listUserMessageWithPaging(String ownUserId, String otherUserId, int pageSize, int pageNum) {
        Page<MessageDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MessageDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MessageDO::isDeleted, false).and(
                condition1 -> condition1.or(condition2 -> condition2.eq(MessageDO::getFromUserId, ownUserId).eq(MessageDO::getToUserId, otherUserId))
                        .or(condition3 -> condition3.eq(MessageDO::getFromUserId, otherUserId).eq(MessageDO::getToUserId, ownUserId))).orderByDesc(MessageDO::getCreatedAt);
        List<MessageDO> messageList = messageMapper.selectPage(page, lambdaQueryWrapper).getRecords();
        return messageList == null ? new ArrayList<>() : messageList;
    }

    @Override
    public List<MessageDO> listUserMessageNotRead(String ownUserId, String otherUserId) {
        List<String> messageIdList = redisTools.listUnreadUserMessageId(ownUserId, otherUserId);
        List<MessageDO> messageList = Collections.synchronizedList(new ArrayList<>());
        for (String messageId : messageIdList) {
            if (redisTools.isKeyExist("message:" + messageId)) {
                messageList.add(redisTools.getMessage(messageId));
            } else {
                MessageDO message = messageMapper.selectById(messageId);
                messageList.add(message);
                redisTools.saveMessage(message);
            }
        }
        redisTools.removeUnreadUserMessageRecord(ownUserId, otherUserId);
        return messageList;
    }

    @Override
    public List<MessageDO> listGroupMessageWithPaging(String groupId, int pageSize, int pageNum) {
        Page<MessageDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MessageDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MessageDO::getToGroupId, groupId).eq(MessageDO::isDeleted, false).orderByDesc(MessageDO::getCreatedAt);
        return messageMapper.selectPage(page, lambdaQueryWrapper).getRecords();
    }

    @Override
    public List<MessageDO> listGroupMessageNotRead(String userId, String groupId) {
        List<String> messageIdList = redisTools.listUnreadGroupMessageId(userId, groupId);
        List<MessageDO> messageList = Collections.synchronizedList(new ArrayList<>());
        for (String messageId : messageIdList) {
            if (redisTools.isKeyExist("message:" + messageId)) {
                messageList.add(redisTools.getMessage(messageId));
            } else {
                MessageDO message = messageMapper.selectById(messageId);
                messageList.add(message);
                redisTools.saveMessage(message);
            }
        }
        redisTools.removeUnreadGroupMessageRecord(userId, groupId);
        return messageList;
    }

    @Override
    public void removeMember(String groupId, String userId) {
        LocalDateTime time = LocalDateTime.now();
        memberMapper.removeMember(userId, groupId, time);
        groupMapper.updateMemberCount(groupId, -1, time);
        log.info("The user: {} leaves the group: {}.", userId, groupId);
    }

    @Override
    public void removeGroup(String userId, String groupId) {
        groupMapper.removeGroup(groupId);
        log.info("The user: {} disbands the group: {}.", userId, groupId);
    }
}