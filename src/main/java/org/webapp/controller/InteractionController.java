package org.webapp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.webapp.pojo.*;
import org.webapp.service.InteractionService;
import org.webapp.service.UserService;
import org.webapp.service.VideoService;
import org.webapp.utils.JwtUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 互动模块
 */
@RestController
public class InteractionController {
    @Autowired
    private InteractionService interactionService;
    @Autowired
    private UserService userService;
    @Autowired
    private VideoService videoService;

    /**
     * 视频点赞/点踩
     *
     * @param videoId    视频 ID
     * @param actionType 操作类型（0-点赞，1-取消点赞，2-点踩，3-取消点踩）
     * @param token      令牌
     */
    @PostMapping(value = "/like/action", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Transactional
    public ResponseVO saveVideoLike(@RequestParam("video_id") String videoId, @RequestParam("action_type") int actionType, @RequestHeader("Access-Token") String token) {
        ResponseVO response;
        String userId = JwtUtils.getUserId(token);
        if (actionType < 0 || actionType > 3) {
            response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        } else if (videoService.getVideo(videoId) == null) {
            response = new ResponseVO(StatusCode.NONEXISTENT_VIDEO, StatusMessage.NONEXISTENT_VIDEO);
        } else {
            LikeDO record = interactionService.getLikeToVideo(userId, videoId);
            if ((actionType & 1) == 0 && (record == null || record.isDeleted())) {
                if (record == null) {
                    interactionService.saveLikeToVideo(userId, videoId, actionType >> 1);
                } else {
                    interactionService.updateLikeToVideo(userId, videoId, actionType);
                }
                if (actionType == 0) {
                    videoService.updateLikeCount(videoId, 1);
                }
                response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
            } else if (record != null && !record.isDeleted() && ((actionType == 1 && !record.isDisliked()) || (actionType == 3 && record.isDisliked()))) {
                //当前存在点赞/点踩记录，且操作为取消点赞/取消点踩
                interactionService.updateLikeToVideo(userId, videoId, actionType);
                if (actionType == 1) {
                    videoService.updateLikeCount(videoId, -1);
                }
                response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
            } else {
                response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
            }
        }
        return response;
    }


    /**
     * 点赞列表
     * 用户点赞视频列表
     *
     * @param userId   用户 ID
     * @param pageSize 页面尺寸
     * @param pageNum  页码
     */
    @GetMapping("/like/list")
    @Transactional(readOnly = true)
    public ResponseVO listLikeToVideo(@RequestParam("user_id") String userId, @RequestParam("page_size") int pageSize, @RequestParam("page_num") int pageNum) {
        ResponseVO response;
        if (pageSize <= 0 || pageSize > 100 || pageNum <= 0) {
            response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        } else if (userService.getUserById(userId) == null) {
            response = new ResponseVO(StatusCode.NONEXISTENT_USER, StatusMessage.NONEXISTENT_USER);
        } else {
            List<VideoDO> videoList = interactionService.listLikeToVideo(userId, pageSize, pageNum);
            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, videoList);
        }
        return response;
    }


    /**
     * 发布评论
     * 视频 ID 和评论 ID 仅允许提供其中一个
     *
     * @param videoId   视频 ID
     * @param commentId 评论 ID
     * @param content   文本
     * @param token     令牌
     */
    @PostMapping(value = "/comment/publish", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Transactional
    public ResponseVO saveComment(@RequestParam("video_id") String videoId, @RequestParam("comment_id") String commentId, @RequestParam("content") String content, @RequestHeader("Access-Token") String token) {
        ResponseVO response;
        String userId = JwtUtils.getUserId(token);
        if (videoId.isEmpty() == commentId.isEmpty()) {
            response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        } else if (videoId.isEmpty()) {
            CommentDO comment = interactionService.getComment(commentId);
            if (comment == null) {
                response = new ResponseVO(StatusCode.NONEXISTENT_COMMENT, StatusMessage.NONEXISTENT_COMMENT);
            } else {
                interactionService.saveComment(userId, comment.getVideoId(), commentId, content);
                videoService.updateCommentCount(comment.getVideoId(), 1);
                response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
            }
        } else {
            if (videoService.getVideo(videoId) == null) {
                response = new ResponseVO(StatusCode.NONEXISTENT_VIDEO, StatusMessage.NONEXISTENT_VIDEO);
            } else {
                interactionService.saveComment(userId, videoId, "none", content);
                videoService.updateCommentCount(videoId, 1);
                response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
            }
        }
        return response;
    }


    /**
     * 评论列表
     * 视频 ID 和评论 ID 仅允许提供其中一个
     *
     * @param videoId   视频 ID
     * @param commentId 评论 ID
     * @param pageSize  页面尺寸
     * @param pageNum   页码
     */
    @GetMapping("/comment/list")
    @Transactional(readOnly = true)
    public ResponseVO listComment(@RequestParam("video_id") String videoId, @RequestParam("comment_id") String commentId, @RequestParam("page_size") int pageSize, @RequestParam("page_num") int pageNum) {
        ResponseVO response;
        if (pageSize <= 0 || pageSize > 100 || pageNum <= 0 || videoId.isEmpty() == commentId.isEmpty()) {
            response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        } else if (videoId.isEmpty()) {
            CommentDO comment = interactionService.getComment(commentId);
            if (comment == null) {
                response = new ResponseVO(StatusCode.NONEXISTENT_COMMENT, StatusMessage.NONEXISTENT_COMMENT);
            } else {
                // TODO: 按回复顺序分页展示子评论
                int total = interactionService.countComment(commentId);
                List<CommentDO> commentList = new ArrayList<>();
                if (pageSize * (pageNum - 1) < total) {
                    commentList = interactionService.listCommentByCommentIdWithPaging(commentId, pageSize, pageNum);
                }
                response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, commentList, (long) total);
            }
        } else {
            VideoDO video = videoService.getVideo(videoId);
            if (video == null) {
                response = new ResponseVO(StatusCode.NONEXISTENT_VIDEO, StatusMessage.NONEXISTENT_VIDEO);
            } else {
                // TODO: 按主评论分页并对当前页主评论按回复顺序展示所有子评论
                Page<CommentDO> commentList = interactionService.listCommentByVideoIdWithPaging(videoId, pageSize, pageNum);
                response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, commentList.getRecords(), commentList.getTotal());
            }
        }
        return response;
    }

    /**
     * 删除评论
     * 包括子评论
     *
     * @param commentId 评论 ID
     */
    @DeleteMapping(value = "/comment/delete", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Transactional
    public ResponseVO removeComment(@RequestParam("comment_id") String commentId) {
        ResponseVO response;
        CommentDO comment = interactionService.getComment(commentId);
        if (comment == null) {
            response = new ResponseVO(StatusCode.NONEXISTENT_COMMENT, StatusMessage.NONEXISTENT_COMMENT);
        } else {
            int total = interactionService.countComment(commentId);
            interactionService.removeComment(commentId, total);
            videoService.updateCommentCount(comment.getVideoId(), -total);
            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
        }
        return response;
    }
}