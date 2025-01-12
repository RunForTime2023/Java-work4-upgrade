package org.webapp.controller;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.webapp.config.JakartaEndpointConfig;
import org.webapp.handler.JakartaWebSocketDecoder;
import org.webapp.handler.JakartaWebSocketEncoder;
import org.webapp.pojo.*;
import org.webapp.service.ChatService;
import org.webapp.service.ContactService;
import org.webapp.service.UserService;
import org.webapp.utils.JwtUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint(value = "/chat", encoders = JakartaWebSocketEncoder.class, decoders = JakartaWebSocketDecoder.class, configurator = JakartaEndpointConfig.class)
public class JakartaWebSocketServer {
    private static Map<String, Session> sessionList = new ConcurrentHashMap<>();
    private static UserService userService;
    private static ContactService contactService;
    private static ChatService chatService;
    private String userId;

    @Autowired
    public void setApplicationContext(UserService userService, ContactService contactService, ChatService chatService) {
        JakartaWebSocketServer.userService = userService;
        JakartaWebSocketServer.contactService = contactService;
        JakartaWebSocketServer.chatService = chatService;
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        this.userId = JwtUtils.getUserId(config.getUserProperties().get("Access-Token").toString());
        sessionList.put(this.userId, session);
        log.info("The session: {} is created successfully. The user: {} is in the chatroom.", session.getId(), this.userId);
        ResponseVO response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
        session.getAsyncRemote().sendObject(response);
    }

    @OnMessage(maxMessageSize = 1000000)
    public void onMessage(MessageDTO clientMessage, Session session) {
        ResponseVO response;
        switch (clientMessage.getActionType()) {
            // 单聊发送消息
            case 1 -> {
                if (this.userId.equals(clientMessage.getUserOrGroupId())) {
                    response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
                } else if (userService.getUserById(userId) == null) {
                    response = new ResponseVO(StatusCode.NONEXISTENT_USER, StatusMessage.NONEXISTENT_USER);
                } else {
                    LikeDO dislike = contactService.getFollow(clientMessage.getUserOrGroupId(), this.userId);
                    if (dislike != null && dislike.isDisliked() && !dislike.isDeleted()) {
                        response = new ResponseVO(StatusCode.BLOCKED_BY_OTHER, StatusMessage.BLOCKED_BY_OTHER);
                    } else {
                        // TODO:图片发送失败时提示错误信息
                        MessageDO message = chatService.saveMessage(this.userId, clientMessage.getUserOrGroupId(), "none", clientMessage.getContent(), clientMessage.getImage());
                        response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, List.of(message));
                    }
                }
            }
            // 单聊消息记录（分页）
            case 2 -> {
                int pageSize = clientMessage.getPageSize(), pageNum = clientMessage.getPageNum();
                if (pageSize <= 0 || pageSize > 100 || pageNum <= 0) {
                    response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
                } else {
                    UserVO user = userService.getUserById(clientMessage.getUserOrGroupId());
                    if (user == null) {
                        response = new ResponseVO(StatusCode.NONEXISTENT_USER, StatusMessage.NONEXISTENT_USER);
                    } else {
                        List<MessageDO> messageList = chatService.listUserMessageWithPaging(this.userId, clientMessage.getUserOrGroupId(), pageSize, pageNum);
                        response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, messageList);
                    }
                }
            }
            // 单聊未读消息记录
            case 3 -> {
                UserVO user = userService.getUserById(clientMessage.getUserOrGroupId());
                if (user == null) {
                    response = new ResponseVO(StatusCode.NONEXISTENT_USER, StatusMessage.NONEXISTENT_USER);
                } else {
                    List<MessageDO> messageList = chatService.listUserMessageNotRead(this.userId, clientMessage.getUserOrGroupId());
                    response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, messageList, (long) messageList.size());
                }
            }
            // 创建群聊
            case 4 -> {
                List<String> memberList = new ArrayList<>();
                memberList.add(this.userId);
                GroupDO group = chatService.saveGroup(this.userId + "于" + LocalDateTime.now() + "创建的群", this.userId, memberList);
                response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, List.of(group));
            }
            // 加入群聊
            case 5 -> {
                GroupDO group = chatService.getGroup(clientMessage.getUserOrGroupId());
                if (group == null) {
                    response = new ResponseVO(StatusCode.NONEXISTENT_GROUP, StatusMessage.NONEXISTENT_GROUP);
                } else {
                    MemberDO member = chatService.getMember(userId, clientMessage.getUserOrGroupId());
                    if (member == null) {
                        chatService.saveMember(this.userId, clientMessage.getUserOrGroupId());
                        response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
                    } else {
                        response = new ResponseVO(StatusCode.IN_GROUP, StatusMessage.IN_GROUP);
                    }
                }
            }
            // 群聊发送消息
            case 6 -> {
                GroupDO group = chatService.getGroup(clientMessage.getUserOrGroupId());
                if (group == null) {
                    response = new ResponseVO(StatusCode.NONEXISTENT_GROUP, StatusMessage.NONEXISTENT_GROUP);
                } else {
                    MemberDO member = chatService.getMember(this.userId, clientMessage.getUserOrGroupId());
                    if (member == null) {
                        response = new ResponseVO(StatusCode.NOT_IN_GROUP, StatusMessage.NOT_IN_GROUP);
                    } else {
                        MessageDO message = chatService.saveMessage(this.userId, "none", clientMessage.getUserOrGroupId(), clientMessage.getContent(), clientMessage.getImage());
                        response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, List.of(message));
                    }
                }
            }
            // 群聊消息记录（分页）
            case 7 -> {
                int pageSize = clientMessage.getPageSize(), pageNum = clientMessage.getPageNum();
                if (pageSize <= 0 || pageSize > 100 || pageNum <= 0) {
                    response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
                } else {
                    GroupDO group = chatService.getGroup(clientMessage.getUserOrGroupId());
                    if (group == null) {
                        response = new ResponseVO(StatusCode.NONEXISTENT_GROUP, StatusMessage.NONEXISTENT_GROUP);
                    } else {
                        MemberDO member = chatService.getMember(this.userId, clientMessage.getUserOrGroupId());
                        if (member == null) {
                            response = new ResponseVO(StatusCode.NOT_IN_GROUP, StatusMessage.NOT_IN_GROUP);
                        } else {
                            List<MessageDO> messageList = chatService.listGroupMessageWithPaging(clientMessage.getUserOrGroupId(), pageSize, pageNum);
                            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, messageList);
                        }
                    }
                }
            }
            // 群聊未读消息记录（分页）
            case 8 -> {
                int pageSize = clientMessage.getPageSize(), pageNum = clientMessage.getPageNum();
                if (pageSize <= 0 || pageSize > 100 || pageNum <= 0) {
                    response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
                } else {
                    GroupDO group = chatService.getGroup(clientMessage.getUserOrGroupId());
                    if (group == null) {
                        response = new ResponseVO(StatusCode.NONEXISTENT_GROUP, StatusMessage.NONEXISTENT_GROUP);
                    } else {
                        MemberDO member = chatService.getMember(this.userId, clientMessage.getUserOrGroupId());
                        if (member == null) {
                            response = new ResponseVO(StatusCode.NOT_IN_GROUP, StatusMessage.NOT_IN_GROUP);
                        } else {
                            List<MessageDO> messageList = chatService.listGroupMessageNotRead(this.userId, clientMessage.getUserOrGroupId());
                            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, messageList);
                        }
                    }
                }
            }
            default -> response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        }
        session.getAsyncRemote().sendObject(response);
    }

    @OnClose
    public void onClose(Session session) {
        sessionList.remove(this.userId);
        log.info("The session: {} is closed. The user: {} is out of the chatroom.", session.getId(), this.userId);
    }

    @OnError
    public void onError(Session session, Throwable e) {
        log.error("There is some error in the session: {}.", session.getId(), e);
        ResponseVO response = new ResponseVO(StatusCode.UNKNOWN_ERROR, StatusMessage.UNKNOWN_ERROR);
        session.getAsyncRemote().sendObject(response);
    }
}
