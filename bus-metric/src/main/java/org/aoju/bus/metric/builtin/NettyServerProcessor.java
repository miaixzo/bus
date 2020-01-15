package org.aoju.bus.metric.builtin;

import io.netty.channel.Channel;
import org.aoju.bus.metric.builtin.ChannelMessage;

public interface NettyServerProcessor {

    void process(Channel channel, ChannelMessage msg);

}