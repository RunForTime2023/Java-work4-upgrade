package org.webapp.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.webapp.pojo.ResponseVO;
import org.webapp.pojo.StatusCode;
import org.webapp.pojo.StatusMessage;
import org.webapp.utils.JwtUtils;

@Slf4j
public class RequestVerifyHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final String wsUri;

    public RequestVerifyHandler(String wsUri) {
        this.wsUri = wsUri;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest msg) throws Exception {
        if (!wsUri.equals(msg.uri())) {
            log.warn("Someone try to send an invalid http request.");
            channelHandlerContext.close();
        }
        String userId = JwtUtils.getUserId(msg.headers().get("Access-Token"));
        if ("anonymous".equals(userId)) {
            channelHandlerContext.channel().close();
        } else {
            String channelId = channelHandlerContext.channel().id().asShortText();
            channelHandlerContext.channel().attr(AttributeKey.valueOf(channelId)).set(userId);
            channelHandlerContext.pipeline().remove(this);
            channelHandlerContext.fireChannelRead(msg.retain());
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable ex) throws Exception {
        channelHandlerContext.close();
        log.error("There is some error.", ex);
        ResponseVO response = new ResponseVO(StatusCode.UNKNOWN_ERROR, StatusMessage.UNKNOWN_ERROR);
        ObjectMapper objectMapper = new ObjectMapper();
        channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(response)));
    }
}
