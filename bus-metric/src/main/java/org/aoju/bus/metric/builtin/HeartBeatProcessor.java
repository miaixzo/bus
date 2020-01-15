package org.aoju.bus.metric.builtin;

import io.netty.channel.Channel;
import org.aoju.bus.logger.Logger;

public class HeartBeatProcessor extends AbstractNettyServerProcessor {

    @Override
    public void process(Channel channel, ChannelMessage msg) {
        Logger.debug("收到心跳包,app:{},channel:{}", msg.getApp(), channel.remoteAddress());
        ChannelContext.saveChannel(msg.getApp(), channel);
    }

}
