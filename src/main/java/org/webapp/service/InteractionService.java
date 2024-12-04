package org.webapp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.webapp.pojo.CommentDO;
import org.webapp.pojo.LikeDO;
import org.webapp.pojo.VideoDO;

import java.util.List;

public interface InteractionService {
    void saveComment(String userId, String videoId, String parentId, String content);

    void saveLikeToVideo(String fromUserId, String toVideoId, int isDisliked);

    void saveLikeToComment(String fromUserId, String toCommentId, int isDisliked);

    CommentDO getComment(String commentId);

    LikeDO getLikeToVideo(String fromUserId, String toVideoId);

    LikeDO getLikeToComment(String fromUserId, String toCommentId);

    List<VideoDO> listLikeToVideo(String fromUserId, int pageSize, int pageNum);

    Page<CommentDO> listCommentByVideoIdWithPaging(String videoId, int pageSize, int pageNum);

    List<CommentDO> listCommentByCommentIdWithPaging(String commentId, int pageSize, int pageNum);

    int countComment(String commentId);

    void updateLikeToVideo(String fromUserId, String toVideoId, int actionType);

    void updateLikeToComment(String fromUserId, String toCommentId, int actionType);

    void updateComment(String commentId, int plus);

    void removeComment(String commentId, int minus);
}
