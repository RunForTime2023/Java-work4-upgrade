package org.webapp.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.webapp.pojo.ResponseVO;
import org.webapp.pojo.StatusCode;
import org.webapp.pojo.StatusMessage;
import org.webapp.pojo.UserVO;
import org.webapp.service.UserService;
import org.webapp.utils.CustomizeUtils;
import org.webapp.utils.FileUtils;
import org.webapp.utils.JwtUtils;

import java.util.List;


/**
 * 用户模块
 */
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 注册
     *
     * @param username 账号
     * @param password 密码
     */
    @PostMapping(value = "/user/register", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Transactional
    public ResponseVO register(@RequestParam String username, @RequestParam String password) {
        ResponseVO response;
        if (CustomizeUtils.isAuthParamValid(username, password)) {
            response = new ResponseVO(StatusCode.MISMATCH_USERNAME_OR_PASSWORD, StatusMessage.MISMATCH_USERNAME_OR_PASSWORD);
        } else if (userService.getUserByUsername(username) != null) {
            response = new ResponseVO(StatusCode.EXIST_USERNAME, StatusMessage.EXIST_USERNAME);
        } else {
            userService.saveUser(username, password);
            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
        }
        return response;
    }

    /**
     * 登录
     *
     * @param username 账号
     * @param password 密码
     */
    @PostMapping(value = "/user/login", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseVO login(@RequestParam String username, @RequestParam String password) {
        ResponseVO response;
        if (CustomizeUtils.isAuthParamValid(username, password)) {
            response = new ResponseVO(StatusCode.WRONG_USERNAME_OR_PASSWORD, StatusMessage.WRONG_USERNAME_OR_PASSWORD);
        } else {
            UserVO user = userService.validateUser(username, password);
            if (user == null) {
                response = new ResponseVO(StatusCode.WRONG_USERNAME_OR_PASSWORD, StatusMessage.WRONG_USERNAME_OR_PASSWORD);
            } else {
                String token = JwtUtils.generateToken(user.getUserId(), username);
                response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, List.of(user), token);
            }
        }
        return response;
    }

    /**
     * 用户信息
     *
     * @param userId 用户 ID
     */
    @GetMapping("/user/info")
    public ResponseVO getUserInfo(@RequestParam("user_id") String userId) {
        UserVO user = userService.getUserById(userId);
        ResponseVO response;
        if (user == null) {
            response = new ResponseVO(StatusCode.NONEXISTENT_USER, StatusMessage.NONEXISTENT_USER);
        } else {
            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, List.of(user));
        }
        return response;
    }

    /**
     * 上传头像
     * 仅支持上传单个文件，文件大小不超过 10 MB
     *
     * @param file  文件
     * @param token 令牌
     */
    @PutMapping(value = "/user/avatar/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseVO saveAvatar(@RequestParam("data") MultipartFile file, @RequestHeader("Access-Token") String token, HttpServletResponse httpServletResponse) {
        ResponseVO response;
        String userId = JwtUtils.getUserId(token);
        if (file.isEmpty()) {
            response = new ResponseVO(StatusCode.EMPTY_FILE, StatusMessage.EMPTY_FILE);
        } else if (file.getSize() > 10485760L) {
            response = new ResponseVO(StatusCode.TOO_LARGE_FILE, StatusMessage.TOO_LARGE_FILE);
        } else if (!FileUtils.isImage(file, userId)) {
            response = new ResponseVO(StatusCode.WRONG_FILE_FORMAT, StatusMessage.WRONG_FILE_FORMAT);
            httpServletResponse.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        } else {
            String avatarUrl = FileUtils.saveAvatar(file, userId);
            userService.updateAvatarUrl(userId, avatarUrl);
            UserVO user = userService.getUserById(userId);
            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, List.of(user));
        }
        return response;
    }

    /**
     * 授权
     * 将用户权限提升至管理员
     *
     * @param actionType 操作类型（0-授权，1-取消授权）
     * @param userId     用户 ID
     */
    @PostMapping(value = "/user/auth", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Transactional
    public ResponseVO updateUserRole(@RequestParam("action_type") int actionType, @RequestParam("user_id") String userId) {
        ResponseVO response;
        UserVO user = userService.getUserById(userId);
        if (actionType < 0 || actionType > 1) {
            response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        } else if (user == null) {
            response = new ResponseVO(StatusCode.NONEXISTENT_USER, StatusMessage.NONEXISTENT_USER);
        } else {
            //TODO:避免重复授权
            userService.updateRole(userId, 1 - actionType);
            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
        }
        return response;
    }
}