package org.aoju.bus.metric.builtin;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.aoju.bus.logger.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HeartBeatServerHandler extends ChannelInboundHandlerAdapter {

    private static Map<ChannelId, AtomicInteger> channelLossConnectCount = new ConcurrentHashMap<>(8);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                Channel channel = ctx.channel();
                AtomicInteger lossCount = channelLossConnectCount.get(channel.id());
                if (lossCount == null) {
                    lossCount = new AtomicInteger();
                    channelLossConnectCount.put(channel.id(), lossCount);
                }
                int count = lossCount.incrementAndGet();
                Logger.warn("No message from the client was received for 5 seconds, {}", channel.remoteAddress());
                if (count > 2) {
                    Logger.warn("Close the inactive channel,{}", channel.remoteAddress());
                    channelLossConnectCount.remove(channel.id());
                    ChannelContext.removeChannel(channel);
                    channel.close();
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}