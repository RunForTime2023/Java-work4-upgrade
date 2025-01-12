package org.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.webapp.pojo.*;
import org.webapp.service.ContactService;
import org.webapp.service.UserService;
import org.webapp.utils.JwtUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 社交模块
 */
@RestController
public class ContactController {
    @Autowired
    private ContactService contactService;
    @Autowired
    private UserService userService;

    /**
     * 关注/屏蔽
     *
     * @param actionType 操作类型（0-关注，1-取消关注，2-屏蔽，3-取消屏蔽）
     * @param toUserId   用户 ID
     * @param token      令牌
     */
    @PostMapping(value = "/relation/action", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Transactional
    public ResponseVO saveFollow(@RequestParam("action_type") int actionType, @RequestParam("to_user_id") String toUserId, @RequestHeader("Access-Token") String token) {
        ResponseVO response;
        String userId = JwtUtils.getUserId(token);
        if (actionType < 0 || actionType > 3 || userId.equals(toUserId)) {
            response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        } else if (userService.getUserById(toUserId) == null) {
            response = new ResponseVO(StatusCode.NONEXISTENT_USER, StatusMessage.NONEXISTENT_USER);
        } else {
            LikeDO record = contactService.getFollow(userId, toUserId);
            if ((actionType & 1) == 0 && (record == null || record.isDeleted())) {
                if (record == null) {
                    contactService.saveFollow(userId, toUserId, actionType >> 1);
                } else {
                    contactService.updateFollow(userId, toUserId, actionType);
                }
                response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
            } else if (record != null && !record.isDeleted() && ((actionType == 1 && !record.isDisliked()) || (actionType == 3 && record.isDisliked()))) {
                //当前存在关注/屏蔽记录，且操作为取消关注/取消屏蔽
                contactService.updateFollow(userId, toUserId, actionType);
                response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
            } else {
                response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
            }
        }
        return response;
    }

    /**
     * 关注列表
     *
     * @param userId   用户 ID
     * @param pageSize 页面尺寸
     * @param pageNum  页码
     */
    @GetMapping("/following/list")
    @Transactional(readOnly = true)
    public ResponseVO listFollow(@RequestParam("user_id") String userId, @RequestParam("page_size") int pageSize, @RequestParam("page_num") int pageNum) {
        ResponseVO response;
        if (pageSize <= 0 || pageSize > 100 || pageNum <= 0) {
            response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        } else {
            UserVO user = userService.getUserById(userId);
            if (user == null) {
                response = new ResponseVO(StatusCode.NONEXISTENT_USER, StatusMessage.NONEXISTENT_USER);
            } else {
                Long total = contactService.countFollow(userId);
                List<UserVO> followingList = new ArrayList<>();
                if ((long) pageSize * (pageNum - 1) < total) {
                    followingList = contactService.listFollowWithPaging(userId, pageSize, pageNum);
                }
                response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, followingList, total);
            }
        }
        return response;
    }

    /**
     * 粉丝列表
     *
     * @param userId   用户 ID
     * @param pageSize 页面尺寸
     * @param pageNum  页码
     */
    @GetMapping("/follower/list")
    @Transactional(readOnly = true)
    public ResponseVO listFans(@RequestParam("user_id") String userId, @RequestParam("page_size") int pageSize, @RequestParam("page_num") int pageNum) {
        ResponseVO response;
        if (pageSize <= 0 || pageSize > 100 || pageNum <= 0) {
            response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        } else {
            UserVO user = userService.getUserById(userId);
            if (user == null) {
                response = new ResponseVO(StatusCode.NONEXISTENT_USER, StatusMessage.NONEXISTENT_USER);
            } else {
                Long total = contactService.countFans(userId);
                List<UserVO> fansList = new ArrayList<>();
                if ((long) pageSize * (pageNum - 1) < total) {
                    fansList = contactService.listFansWithPaging(userId, pageSize, pageNum);
                }
                response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, fansList, total);
            }
        }
        return response;
    }

    /**
     * 好友列表
     * 与当前登录用户相互关注的用户列表
     *
     * @param pageSize 页面尺寸
     * @param pageNum  页码
     * @param token    令牌
     */
    @GetMapping("/friends/list")
    @Transactional(readOnly = true)
    public ResponseVO listFriend(@RequestParam("page_size") int pageSize, @RequestParam("page_num") int pageNum, @RequestHeader("Access-Token") String token) {
        ResponseVO response;
        if (pageSize <= 0 || pageSize > 100 || pageNum <= 0) {
            response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        } else {
            String userId = JwtUtils.getUserId(token);
            Long total = contactService.countFriend(userId);
            List<UserVO> friendsList = new ArrayList<>();
            if ((long) pageSize * (pageNum - 1) < total) {
                friendsList = contactService.listFriendWithPaging(userId, pageSize, pageNum);
            }
            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, friendsList, total);
        }
        return response;
    }

    /**
     * 黑名单
     * 被当前登录用户屏蔽的用户列表
     *
     * @param pageSize 页面尺寸
     * @param pageNum  页码
     * @param token    令牌
     */
    @GetMapping("/block/list")
    @Transactional(readOnly = true)
    public ResponseVO listBlock(@RequestParam("page_size") int pageSize, @RequestParam("page_num") int pageNum, @RequestHeader("Access-Token") String token) {
        ResponseVO response;
        if (pageSize <= 0 || pageSize > 100 || pageNum <= 0) {
            response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        } else {
            String userId = JwtUtils.getUserId(token);
            Long total = contactService.countBlock(userId);
            List<UserVO> blockList = new ArrayList<>();
            if ((long) pageSize * (pageNum - 1) < total) {
                blockList = contactService.listBlockWithPaging(userId, pageSize, pageNum);
            }
            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, blockList, total);
        }
        return response;
    }
}