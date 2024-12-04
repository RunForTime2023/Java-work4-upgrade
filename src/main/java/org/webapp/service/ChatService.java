package org.webapp.service;

import org.webapp.pojo.GroupDO;
import org.webapp.pojo.MemberDO;
import org.webapp.pojo.MessageDO;

import java.util.List;

public interface ChatService {
    MessageDO saveMessage(String fromUserId, String toUserId, String toGroupId, String content, String image);

    GroupDO saveGroup(String groupName, String userId, List<String> memberList);

    void saveMember(String userId, String groupId);

    GroupDO getGroup(String groupId);

    MemberDO getMember(String userId, String groupId);

    List<MessageDO> listUserMessageWithPaging(String ownUserId, String otherUserId, int pageSize, int pageNum);

    List<MessageDO> listUserMessageNotRead(String ownUserId, String otherUserId);

    List<MessageDO> listGroupMessageWithPaging(String groupId, int pageSize, int pageNum);

    List<MessageDO> listGroupMessageNotRead(String userId, String groupId);

    void removeMember(String userId, String groupId);

    void removeGroup(String userId, String groupId);
}