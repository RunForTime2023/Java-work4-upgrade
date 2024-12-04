package org.webapp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.webapp.mapper.LikeMapper;
import org.webapp.pojo.LikeDO;
import org.webapp.pojo.UserVO;

import java.util.List;

@Slf4j
@Service
public class ContactServiceImpl implements ContactService {
    @Autowired
    private LikeMapper likeMapper;

    @Override
    public void saveFollow(String fromUserId, String toUserId, int isDisliked) {
        LikeDO record = new LikeDO(fromUserId, toUserId, "none", "none");
        if (isDisliked == 0) {
            likeMapper.insert(record);
            log.info("The user: {} follows the user: {}.", fromUserId, toUserId);
        } else {
            record.setDisliked(true);
            likeMapper.insert(record);
            log.info("The user: {} blocks the user: {}.", fromUserId, toUserId);
        }
    }

    @Override
    public LikeDO getFollow(String fromUserId, String toUserId) {
        LambdaQueryWrapper<LikeDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(LikeDO::getFromUserId, fromUserId).eq(LikeDO::getToUserId, toUserId);
        return likeMapper.selectOne(lambdaQueryWrapper);
    }

    @Override
    public List<UserVO> listFollowWithPaging(String fromUserId, int pageSize, int pageNum) {
        return likeMapper.listFollow(fromUserId, pageSize * (pageNum - 1), pageSize);
    }

    @Override
    public Long countFollow(String fromUserId) {
        LambdaQueryWrapper<LikeDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(LikeDO::getFromUserId, fromUserId).ne(LikeDO::getToUserId, "none").eq(LikeDO::isDisliked, false).eq(LikeDO::isDeleted, false);
        return likeMapper.selectCount(lambdaQueryWrapper);
    }

    @Override
    public List<UserVO> listFansWithPaging(String userId, int pageSize, int pageNum) {
        return likeMapper.listFans(userId, pageSize * (pageNum - 1), pageSize);
    }

    @Override
    public Long countFans(String userId) {
        LambdaQueryWrapper<LikeDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(LikeDO::getToUserId, userId).eq(LikeDO::isDisliked, false).eq(LikeDO::isDeleted, false);
        return likeMapper.selectCount(lambdaQueryWrapper);
    }

    @Override
    public List<UserVO> listFriendWithPaging(String fromUserId, int pageSize, int pageNum) {
        return likeMapper.listFriend(fromUserId, pageSize * (pageNum - 1), pageSize);
    }

    @Override
    public Long countFriend(String fromUserId) {
        return likeMapper.countFriend(fromUserId);
    }

    @Override
    public List<UserVO> listBlockWithPaging(String fromUserId, int pageSize, int pageNum) {
        return likeMapper.listBlock(fromUserId, pageSize * (pageNum - 1), pageSize);
    }

    @Override
    public Long countBlock(String fromUserId) {
        LambdaQueryWrapper<LikeDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(LikeDO::getFromUserId, fromUserId).ne(LikeDO::getToUserId, "none").eq(LikeDO::isDisliked, true).eq(LikeDO::isDeleted, false);
        return likeMapper.selectCount(lambdaQueryWrapper);
    }

    @Override
    public void updateFollow(String fromUserId, String toUserId, int actionType) {
        LambdaUpdateWrapper<LikeDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(LikeDO::isDisliked, actionType >> 1).set(LikeDO::isDeleted, actionType & 1).eq(LikeDO::getFromUserId, fromUserId).eq(LikeDO::getToUserId, toUserId);
        likeMapper.update(lambdaUpdateWrapper);
        switch (actionType) {
            case 0 -> log.info("The user: {} follows the user: {}.", fromUserId, toUserId);
            case 1 -> log.info("The user: {} unfollows the user: {}.", fromUserId, toUserId);
            case 2 -> log.info("The user: {} blocks the user: {}.", fromUserId, toUserId);
            case 3 -> log.info("The user: {} unblocks the user: {}.", fromUserId, toUserId);
        }
    }
}