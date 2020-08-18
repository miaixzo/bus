package org.aoju.bus.goalie.builtin;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;

public abstract class AbstractNettyServerProcessor implements NettyServerProcessor {

    protected void writeAndFlush(Channel channel, String code, Object data) {
        ChannelMessage msg = new ChannelMessage();
        msg.setCode(code);
        msg.setData(JSON.toJSONString(data));
        channel.writeAndFlush(msg);
    }

}
