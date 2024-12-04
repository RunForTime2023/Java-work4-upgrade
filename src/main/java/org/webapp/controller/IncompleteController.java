package org.webapp.controller;

import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.webapp.pojo.*;
import org.webapp.service.*;
import org.webapp.utils.JwtUtils;

import java.util.List;

public class IncompleteController {
    private UserService userService;
    private VideoService videoService;
    private InteractionService interactionService;
    private ContactService contactService;
    private ChatService chatService;

    @DeleteMapping("/user/delete")
    @Transactional
    public ResponseVO removeUser(@RequestHeader("Access-Token") String token) {
        ResponseVO response;
        String userId = JwtUtils.getUserId(token);
        if (userService.getUserById(userId) == null) {
            response = new ResponseVO(StatusCode.NONEXISTENT_USER, StatusMessage.NONEXISTENT_USER);
        } else {
            userService.removeUser(userId);
            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
        }
        return response;
    }

    /**
     * 视频流
     *
     * @param latestTime 13 位时间戳
     */
    @GetMapping("/video/feed")
    public ResponseVO recommendVideo(@RequestParam("latest_time") String latestTime) {
        ResponseVO response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
        return response;
    }

    /**
     * 删除视频
     *
     * @param videoId 视频 ID
     */
    @DeleteMapping(value = "/video/delete", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Transactional
    public ResponseVO removeVideo(@RequestParam("video_id") String videoId) {
        ResponseVO response;
        if (videoService.getVideo(videoId) == null) {
            response = new ResponseVO(StatusCode.NONEXISTENT_VIDEO, StatusMessage.NONEXISTENT_VIDEO);
        } else {
            videoService.removeVideo(videoId);
            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
        }
        return response;
    }

    /**
     * 评论点赞/点踩
     *
     * @param commentId  评论 ID
     * @param actionType 操作类型（0-点赞，1-取消点赞，2-点踩，3-取消点踩）
     * @param token      令牌
     */
    @PostMapping(value = "/comment/like", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Transactional
    public ResponseVO saveCommentLike(@RequestParam("comment_id") String commentId, @RequestParam("action_type") int actionType, @RequestHeader("Access-Token") String token) {
        ResponseVO response;
        String userId = JwtUtils.getUserId(token);
        if (actionType < 0 || actionType > 3) {
            response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        } else if (interactionService.getComment(commentId) == null) {
            response = new ResponseVO(StatusCode.NONEXISTENT_COMMENT, StatusMessage.NONEXISTENT_COMMENT);
        } else {
            LikeDO record = interactionService.getLikeToComment(userId, commentId);
            if ((actionType & 1) == 0 && (record == null || record.isDeleted())) {
                if (record == null) {
                    interactionService.saveLikeToComment(userId, commentId, actionType >> 1);
                } else {
                    interactionService.updateLikeToComment(userId, commentId, actionType);
                }
                if (actionType == 0) {
                    interactionService.updateComment(commentId, 1);
                }
                response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
            } else if (record != null && !record.isDeleted() && ((actionType == 1 && !record.isDisliked()) || (actionType == 3 && record.isDisliked()))) {
                //当前存在点赞/点踩记录，且操作为取消点赞/取消点踩
                interactionService.updateLikeToComment(userId, commentId, actionType);
                if (actionType == 1) {
                    interactionService.updateComment(commentId, -1);
                }
                response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
            } else {
                response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
            }
        }
        return response;
    }

    /**
     * 单聊消息记录
     *
     * @param toUserId 用户 ID
     * @param pageSize 页面尺寸
     * @param pageNum  页码
     * @param token    令牌
     */
    @GetMapping("/chat/record/person")
    @Transactional(readOnly = true)
    public ResponseVO listUserMessage(@RequestParam("to_user_id") String toUserId, @RequestParam("page_size") int pageSize, @RequestParam("page_num") int pageNum, @RequestHeader("Access-Token") String token) {
        ResponseVO response;
        if (pageSize <= 0 || pageSize > 100 || pageNum <= 0) {
            response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        } else {
            String fromUserId = JwtUtils.getUserId(token);
            List<MessageDO> messageList = chatService.listUserMessageWithPaging(fromUserId, toUserId, pageSize, pageNum);
            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, messageList);
        }
        return response;
    }

    /**
     * 创建群聊
     *
     * @param groupName  群名称
     * @param memberList 群成员
     * @param token      令牌
     */
    @PostMapping(value = "/chat/group/create", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Transactional
    public ResponseVO saveGroup(@RequestParam("group_name") String groupName, @RequestParam("member_list") List<String> memberList, @RequestHeader("Access-Token") String token) {
        ResponseVO response;
        String userId = JwtUtils.getUserId(token);
        if (!memberList.contains(userId)) {
            memberList.add(userId);
        }
        if (memberList.size() > 200) {
            response = new ResponseVO(StatusCode.EXCEED_MEMBER_LIMIT, StatusMessage.EXCEED_MEMBER_LIMIT);
        } else {
            GroupDO group = chatService.saveGroup(groupName, userId, memberList);
            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, List.of(group));
        }
        return response;
    }

    /**
     * 加入/退出群聊
     *
     * @param actionType 操作（0-加入，1-退出）
     * @param groupId    群 ID
     * @param token      令牌
     */
    @PostMapping(value = "/chat/group/join", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Transactional
    public ResponseVO saveMember(@RequestParam("action_type") int actionType, @RequestParam("group_id") String groupId, @RequestHeader("Access-Token") String token) {
        ResponseVO response;
        String userId = JwtUtils.getUserId(token);
        GroupDO group = chatService.getGroup(groupId);
        if (actionType < 0 || actionType > 1) {
            response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        } else if (group == null) {
            response = new ResponseVO(StatusCode.NONEXISTENT_GROUP, StatusMessage.NONEXISTENT_GROUP);
        } else {
            MemberDO member = chatService.getMember(userId, groupId);
            if (actionType == 0 && (member == null || member.isDeleted())) {
                chatService.saveMember(userId, groupId);
                response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
            } else if (member != null && !member.isDeleted() && actionType == 1) {
                // 是群主，直接解散群聊
                if (userId.equals(group.getLeaderId())) {
                    chatService.removeGroup(userId, groupId);
                } else {
                    chatService.removeMember(userId, groupId);
                }
                response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
            } else {
                response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
            }
        }
        return response;
    }

    /**
     * 群聊消息记录
     *
     * @param groupId  群 ID
     * @param pageSize 页面尺寸
     * @param pageNum  页码
     */
    @GetMapping("/chat/record/group")
    @Transactional(readOnly = true)
    public ResponseVO listGroupMessage(@RequestParam("group_id") String groupId, @RequestParam("page_size") int pageSize, @RequestParam("page_num") int pageNum) {
        ResponseVO response;
        if (pageSize <= 0 || pageSize > 100 || pageNum <= 0) {
            response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        } else {
            List<MessageDO> messageList = chatService.listGroupMessageWithPaging(groupId, pageSize, pageNum);
            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, messageList);
        }
        return response;
    }

    /**
     * 解散群聊
     *
     * @param groupId 群 ID
     * @param token   令牌
     */
    @DeleteMapping(value = "/chat/group/delete", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Transactional
    public ResponseVO removeGroup(@RequestParam("group_id") String groupId, @RequestHeader("Access-Token") String token) {
        ResponseVO response;
        String userId = JwtUtils.getUserId(token);
        GroupDO group = chatService.getGroup(groupId);
        if (group == null) {
            response = new ResponseVO(StatusCode.NONEXISTENT_GROUP, StatusMessage.NONEXISTENT_GROUP);
        } else if (userId.equals(group.getLeaderId())) {
            chatService.removeGroup(userId, groupId);
            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
        } else {
            response = new ResponseVO(StatusCode.NO_PERMISSION, StatusMessage.NO_PERMISSION);
        }
        return response;
    }
}
