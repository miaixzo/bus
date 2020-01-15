package org.aoju.bus.metric.builtin;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.metric.ApiRegister;

public class ApiConfigServerHandler extends SimpleChannelInboundHandler<ChannelMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChannelMessage configMsg) {
        String code = configMsg.getCode();

        NettyServerProcessor processor = (NettyServerProcessor) ApiRegister.getInstance().require(code);
        if (processor != null) {
            processor.process(ctx.channel(), configMsg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        Logger.info("The client is connected, {}", incoming.remoteAddress());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        Logger.info("Client connection lost, {} ", incoming.remoteAddress());
        ChannelContext.removeChannel(incoming);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logger.error("The server error, {}", cause);
    }

}
