package org.webapp.service;

import org.webapp.pojo.LikeDO;
import org.webapp.pojo.UserVO;

import java.util.List;

public interface ContactService {
    void saveFollow(String fromUserId, String toUserId, int isDisliked);

    LikeDO getFollow(String fromUserId, String toUserId);

    List<UserVO> listFollowWithPaging(String fromUserId, int pageSize, int pageNum);

    Long countFollow(String fromUserId);

    List<UserVO> listFansWithPaging(String userId, int pageSize, int pageNum);

    Long countFans(String userId);

    List<UserVO> listFriendWithPaging(String fromUserId, int pageSize, int pageNum);

    Long countFriend(String fromUserId);

    List<UserVO> listBlockWithPaging(String fromUserId, int pageSize, int pageNum);

    Long countBlock(String fromUserId);

    void updateFollow(String fromUserId, String toUserId, int actionType);
}
