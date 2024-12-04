package org.webapp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.webapp.mapper.CommentMapper;
import org.webapp.mapper.LikeMapper;
import org.webapp.pojo.CommentDO;
import org.webapp.pojo.LikeDO;
import org.webapp.pojo.VideoDO;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class InteractionServiceImpl implements InteractionService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private LikeMapper likeMapper;

    @Override
    public void saveComment(String userId, String videoId, String parentId, String content) {
        CommentDO comment = new CommentDO(userId, videoId, parentId, content);
        commentMapper.insert(comment);
        if (!"none".equals(parentId)) {
            commentMapper.updateCommentCount(parentId);
        }
    }

    @Override
    public void saveLikeToVideo(String fromUserId, String toVideoId, int isDisliked) {
        LikeDO like = new LikeDO(fromUserId, "none", toVideoId, "none");
        if (isDisliked == 0) {
            likeMapper.insert(like);
            log.info("The user: {} gives like to the video: {}.", fromUserId, toVideoId);
        } else {
            like.setDisliked(true);
            likeMapper.insert(like);
            log.info("The user: {} gives dislike to the video: {}.", fromUserId, toVideoId);
        }
    }

    @Override
    public void saveLikeToComment(String fromUserId, String toCommentId, int isDisliked) {
        LikeDO like = new LikeDO(fromUserId, "none", "none", toCommentId);
        if (isDisliked == 0) {
            likeMapper.insert(like);
            log.info("The user: {} gives like to the comment: {}.", fromUserId, toCommentId);
        } else {
            like.setDisliked(true);
            likeMapper.insert(like);
            log.info("The user: {} gives dislike to the comment: {}.", fromUserId, toCommentId);
        }
    }

    @Override
    public CommentDO getComment(String commentId) {
        LambdaQueryWrapper<CommentDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CommentDO::getCommentId, commentId).eq(CommentDO::isDeleted, false);
        return commentMapper.selectOne(lambdaQueryWrapper);
    }

    @Override
    public LikeDO getLikeToVideo(String fromUserId, String toVideoId) {
        LambdaQueryWrapper<LikeDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(LikeDO::getFromUserId, fromUserId).eq(LikeDO::getToVideoId, toVideoId);
        return likeMapper.selectOne(lambdaQueryWrapper);
    }

    @Override
    public LikeDO getLikeToComment(String fromUserId, String toCommentId) {
        LambdaQueryWrapper<LikeDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(LikeDO::getFromUserId, fromUserId).eq(LikeDO::getToCommentId, toCommentId);
        return likeMapper.selectOne(lambdaQueryWrapper);
    }

    @Override
    public List<VideoDO> listLikeToVideo(String fromUserId, int pageSize, int pageNum) {
        return likeMapper.listLikeToVideo(fromUserId, pageSize * (pageNum - 1), pageSize);
    }

    @Override
    public Page<CommentDO> listCommentByVideoIdWithPaging(String videoId, int pageSize, int pageNum) {
        Page<CommentDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CommentDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CommentDO::getVideoId, videoId).eq(CommentDO::isDeleted, false);
        return commentMapper.selectPage(page, lambdaQueryWrapper);
    }

    @Override
    public List<CommentDO> listCommentByCommentIdWithPaging(String commentId, int pageSize, int pageNum) {
        return commentMapper.listCommentWithChild(commentId, pageSize * (pageNum - 1), pageSize);
    }

    @Override
    public int countComment(String commentId) {
        return commentMapper.countComment(commentId);
    }

    @Override
    public void updateLikeToVideo(String fromUserId, String toVideoId, int actionType) {
        LambdaUpdateWrapper<LikeDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(LikeDO::isDisliked, actionType >> 1).set(LikeDO::isDeleted, actionType & 1).eq(LikeDO::getFromUserId, fromUserId).eq(LikeDO::getToCommentId, toVideoId);
        likeMapper.update(lambdaUpdateWrapper);
        switch (actionType) {
            case 0 -> log.info("The user: {} gives like to the video: {}.", fromUserId, toVideoId);
            case 1 -> log.info("The user: {} cancels like to the video: {}.", fromUserId, toVideoId);
            case 2 -> log.info("The user: {} gives dislike to the video: {}.", fromUserId, toVideoId);
            case 3 -> log.info("The user: {} cancels dislike to the video: {}.", fromUserId, toVideoId);
        }
    }

    @Override
    public void updateLikeToComment(String fromUserId, String toCommentId, int actionType) {
        LambdaUpdateWrapper<LikeDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(LikeDO::isDisliked, actionType >> 1).set(LikeDO::isDeleted, actionType & 1).eq(LikeDO::getFromUserId, fromUserId).eq(LikeDO::getToCommentId, toCommentId);
        likeMapper.update(lambdaUpdateWrapper);
        switch (actionType) {
            case 0 -> log.info("The user: {} gives like to the comment: {}.", fromUserId, toCommentId);
            case 1 -> log.info("The user: {} cancels like to the comment: {}.", fromUserId, toCommentId);
            case 2 -> log.info("The user: {} gives dislike to the comment: {}.", fromUserId, toCommentId);
            case 3 -> log.info("The user: {} cancels dislike to the comment: {}.", fromUserId, toCommentId);
        }
    }

    @Override
    public void updateComment(String commentId, int plus) {
        LambdaUpdateWrapper<CommentDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.setIncrBy(CommentDO::getLikeCount, plus).set(CommentDO::getUpdatedAt, LocalDateTime.now()).eq(CommentDO::getCommentId, commentId);
        commentMapper.update(lambdaUpdateWrapper);
    }

    @Override
    public void removeComment(String commentId, int minus) {
        commentMapper.removeComment(commentId, minus);
    }
}
