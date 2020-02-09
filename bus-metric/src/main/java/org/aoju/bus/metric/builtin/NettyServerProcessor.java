package org.aoju.bus.metric.builtin;

import io.netty.channel.Channel;

public interface NettyServerProcessor {

    void process(Channel channel, ChannelMessage msg);

}