package org.webapp.controller;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.webapp.handler.NettyMessageHandler;
import org.webapp.handler.RequestVerifyHandler;

import java.net.InetSocketAddress;

@Slf4j
@Component
public class NettyWebSocketServer {
    private static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private Channel channel;

    @PostConstruct
    public void init() {
        log.info("Netty WebSocket is starting...");
        int port = 10002;
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(eventLoopGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new ChunkedWriteHandler());
                        pipeline.addLast(new HttpObjectAggregator(1048576));
                        pipeline.addLast(new RequestVerifyHandler("/chat"));
                        pipeline.addLast(new WebSocketServerProtocolHandler("/chat", null, false, 1000000));
                        pipeline.addLast(new NettyMessageHandler());
                    }
                })
                // 心跳保活
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_BACKLOG, 512);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
        this.channel = serverBootstrap.bind(inetSocketAddress).syncUninterruptibly().channel();
        log.info("Netty WebSocket starts successfully.");
    }

    @PreDestroy
    public void destroy() {
        if (this.channel != null) {
            this.channel.close();
        }
        eventLoopGroup.shutdownGracefully();
        log.info("Netty WebSocket shutdown successfully.");
    }
}
