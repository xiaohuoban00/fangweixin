package com.zmq.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author zmq
 * @Date 2020/6/9 16:29
 */
@Component
public class WSServer {

    private final Logger logger = LoggerFactory.getLogger(WSServer.class);

    private static class SingleWSServer{
        static final WSServer instance = new WSServer();
    }

    public static WSServer getInstance(){
        return SingleWSServer.instance;
    }

    private EventLoopGroup mainGroup;
    private EventLoopGroup subGroup;
    private ServerBootstrap serverBootstrap;
    private ChannelFuture channelFuture;

    private WSServer(){
        mainGroup = new NioEventLoopGroup();
        subGroup = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(mainGroup,subGroup).channel(NioServerSocketChannel.class).childHandler(new WSServerInitializer());
    }

    public void start(){
        int port = 8088;
        channelFuture = serverBootstrap.bind(port);
        logger.info("netty websocket server 启动完毕...，端口号为"+port);
    }
}
