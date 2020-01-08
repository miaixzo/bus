/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.metric.config;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.MapUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.metric.Config;
import org.aoju.bus.metric.Context;
import org.aoju.bus.metric.builtin.DefinitionHolder;
import org.aoju.bus.metric.consts.NettyMode;
import org.aoju.bus.metric.magic.Api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8++
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<ConfigMessage> {


    private NettyClient nettyClient;
    private Map<String, NettyProcessor> processorMap;

    private String docUrl;

    public NettyClientHandler(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
        this.processorMap = nettyClient.getProcessorMap();
        this.docUrl = nettyClient.getDocUrl();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Logger.info("连接配置中心成功");
        Channel channel = ctx.channel();
        this.registerServer(channel);
        this.syncAppApiInfi(channel);
        this.updateConfigFromServer(channel);
    }

    public void updateConfigFromServer(final Channel channel) {
        Logger.info("----开始同步配置中心数据----");
        try {
            downloadSecretConfig(channel);
            downloadPermissionConfig(channel);
            downloadLimitConfig(channel);
        } catch (Exception e) {
            Logger.error("同步配置中心数据错误", e);
        }
    }

    public void syncAppApiInfi(Channel channel) {
        Logger.info("同步本地API到配置中心");
        ConfigMessage syncMsg = newConfigMsg(NettyMode.SYNC_APP_API);
        List<Api> allApi = DefinitionHolder.listAllApi();
        syncMsg.setData(JSON.toJSONString(allApi));
        channel.writeAndFlush(syncMsg);
    }

    public void registerServer(Channel channel) {
        ConfigMessage msg = newConfigMsg(NettyMode.CLIENT_CONNECTED);
        Config config = Context.getConfig();

        Map map = MapUtils.newHashMap();
        map.put("app", msg.getApp());
        map.put("status", config.isShowDoc() ? (byte) 1 : 0);
        map.put("docUrl", this.docUrl);
        msg.setData(JSON.toJSONString(map));
        channel.writeAndFlush(msg);
    }

    public ConfigMessage newConfigMsg(NettyMode nettyMode) {
        ConfigMessage msg = new ConfigMessage();
        msg.setCode(nettyMode.getCode());
        return msg;
    }

    public void downloadSecretConfig(Channel channel) {
        Logger.info("下载秘钥配置");
        ConfigMessage msg = this.newConfigMsg(NettyMode.DOWNLOAD_SECRET_CONFIG);
        channel.writeAndFlush(msg);
    }

    public void downloadPermissionConfig(Channel channel) {
        Logger.info("下载权限配置");
        ConfigMessage msg = this.newConfigMsg(NettyMode.DOWNLOAD_PERMISSION_CONFIG);
        channel.writeAndFlush(msg);
    }

    public void downloadLimitConfig(Channel channel) {
        Logger.info("下载限流配置");
        ConfigMessage msg = this.newConfigMsg(NettyMode.DOWNLOAD_LIMIT_CONFIG);
        channel.writeAndFlush(msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ConfigMessage configMessage) {
        String code = configMessage.getCode();
        NettyProcessor processor = processorMap.get(code);
        if (processor == null) {
            throw new RuntimeException("错误的code:" + code);
        }
        processor.process(ctx.channel(), configMessage.getData());
    }

    /**
     * 掉线
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final EventLoop eventLoop = ctx.channel().eventLoop();
        /*
         如果按下Ctrl+C，打印：isShutdown:false,isTerminated:false, isShuttingDown:true
         logger.info("isShutdown:{},isTerminated:{}, isShuttingDown:{}", eventLoop.isShutdown(), eventLoop.isTerminated(), eventLoop.isShuttingDown());
         正准备关闭，不需要再重启了
         */
        if (eventLoop.isShuttingDown()) {
            super.channelInactive(ctx);
            return;
        }
        Logger.info("已断开与服务端的链接，尝试重连...");
        eventLoop.schedule(() -> nettyClient.reconnect(eventLoop), 5L, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof InstrumentException) {
            Logger.error("Netty客户端启动失败:", cause);
            ctx.channel().close();
            System.exit(0);
        } else {
            Logger.error("Netty错误", cause);
            super.exceptionCaught(ctx, cause);
        }
    }

}