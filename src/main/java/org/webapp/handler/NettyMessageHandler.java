package org.webapp.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ImmediateEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.webapp.pojo.MessageDTO;
import org.webapp.pojo.ResponseVO;
import org.webapp.pojo.StatusCode;
import org.webapp.pojo.StatusMessage;
import org.webapp.service.ChatService;
import org.webapp.service.ContactService;
import org.webapp.service.UserService;
import org.webapp.utils.CustomizeUtils;

@Slf4j
@Component
public class NettyMessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static ChannelGroup channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
    private static UserService userService;
    private static ContactService contactService;
    private static ChatService chatService;
    private String userId;

    @Autowired
    public void setApplicationContext(UserService userService, ContactService contactService, ChatService chatService) {
        NettyMessageHandler.userService = userService;
        NettyMessageHandler.contactService = contactService;
        NettyMessageHandler.chatService = chatService;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("There is some error in the channel: {} with the client ip: {}.", ctx.channel().id(), ctx.channel().remoteAddress(), cause);
        ResponseVO response = new ResponseVO(StatusCode.UNKNOWN_ERROR, StatusMessage.UNKNOWN_ERROR);
        ObjectMapper objectMapper = new ObjectMapper();
        ctx.channel().writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(response)));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channelGroup.add(ctx.channel());
        log.info("The channel: {} with the client ip: {} is connected.", ctx.channel().id(), ctx.channel().remoteAddress());
    }

    // TODO: 扩展可接收消息最大长度
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        ObjectMapper objectMapper = CustomizeUtils.customizedObjectMapper();
        String channelId = channelHandlerContext.channel().id().asShortText();
        if (this.userId == null) {
            this.userId = channelHandlerContext.channel().attr(AttributeKey.valueOf(channelId)).get().toString();
        }
        MessageDTO message = objectMapper.readValue(textWebSocketFrame.text(), MessageDTO.class);
        ResponseVO response = getResponse(message);
        channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(response)));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channelGroup.remove(ctx.channel());
        log.info("The channel: {} with the client ip: {} is disconnected. The user: {} is offline now.", ctx.channel().id(), ctx.channel().remoteAddress(), this.userId);
    }

    public ResponseVO getResponse(MessageDTO clientMessage) {
        ResponseVO response;
        response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.NO_PERMISSION);
        return response;
    }
}
