package com.zmq.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.time.LocalDateTime;

/**
 * Created by IntelliJ IDEA.
 * 处理消息的handler
 * TextWebSocketFrame:在netty中用来专门处理文本的对象，frame是消息的载体
 *
 * @Author zmq
 * @Date 2020/6/9 17:00
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        //获取客户端传输过来的消息
        String content = msg.text();
        System.out.println("接收到的数据:" + content);
        for (Channel client : clients) {
            client.writeAndFlush(new TextWebSocketFrame("服务器接收到消息的时间为："+ LocalDateTime.now()+"，消息为："+content));
        }
    }

    /**
     * 当客户端连接到服务端后（打开连接）
     * 获取客户端的channel，并且放到ChannelGroup中进行管理
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        clients.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //当触发handlerRemoved,ChannelGroup会自动移除的对应客户端的channel
        //clients.remove(ctx.channel());
        System.out.println("客户端断开，长id"+ctx.channel().id().asLongText());
        System.out.println("客户端断开，短id"+ctx.channel().id().asShortText());
    }
}
