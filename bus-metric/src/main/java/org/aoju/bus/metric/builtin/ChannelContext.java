package org.aoju.bus.metric.builtin;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelContext {

    private static Map<String, ChannelGroup> appChannelGroup = new ConcurrentHashMap<>(8);

    public static synchronized Set<String> listAppNames() {
        Set<String> apps = new HashSet<>();
        for (Map.Entry<String, ChannelGroup> channelGroupEntry : appChannelGroup.entrySet()) {
            if (channelGroupEntry.getValue().size() > 0) {
                apps.add(channelGroupEntry.getKey());
            }
        }
        return apps;
    }

    public static synchronized void writeAndFlush(ChannelMessage msg) {
        ChannelGroup channelGroup = ChannelContext.getChannelGroupByApp(msg.getApp());
        if (channelGroup != null) {
            channelGroup.writeAndFlush(msg);
        }
    }

    public static synchronized void removeChannel(Channel channel) {
        Set<Map.Entry<String, ChannelGroup>> entrySet = appChannelGroup.entrySet();
        for (Map.Entry<String, ChannelGroup> channelGroupEntry : entrySet) {
            ChannelGroup channelGroup = channelGroupEntry.getValue();
            Channel ch = channelGroup.find(channel.id());
            if (ch != null) {
                channelGroup.remove(ch);
            }
            if (channelGroup.isEmpty()) {
                String app = channelGroupEntry.getKey();
                // ApiAware.getBean(ApiInfoService.class).removeApp(app);
            }
        }
    }

    public static synchronized ChannelGroup getChannelGroupByApp(String app) {
        return appChannelGroup.get(app);
    }

    public static synchronized void saveChannel(String app, Channel ch) {
        ChannelGroup channelGroup = appChannelGroup.get(app);
        if (channelGroup == null) {
            channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            appChannelGroup.put(app, channelGroup);
        }
        channelGroup.add(ch);
    }

}
