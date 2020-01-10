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

import org.aoju.bus.logger.Logger;
import org.aoju.bus.metric.Config;
import org.aoju.bus.metric.Context;
import org.aoju.bus.metric.builtin.LocalAppSecretManager;
import org.aoju.bus.metric.builtin.ManagerInitializer;
import org.aoju.bus.metric.config.process.*;
import org.aoju.bus.metric.consts.NettyMode;
import org.aoju.bus.metric.oauth2.ApiPermissionManager;
import org.aoju.bus.metric.oauth2.PermissionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 配置客户端
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8++
 */
public class ConfigClient {

    private Map<String, NettyProcessor> processorMap = new HashMap<>();

    private List<ManagerInitializer> initializers = new ArrayList<>(4);

    private PermissionManager permissionManager = new ApiPermissionManager();
    private LocalAppSecretManager appSecretManager = new LocalAppSecretManager();

    private String host;
    private int port;
    private String docUrl;


    /**
     * @param appName 应用名称
     * @param host    配置中心ip
     * @param port    配置中心端口
     */
    public ConfigClient(String appName, String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * @param appName 应用名称
     * @param host    配置中心ip
     * @param port    配置中心端口
     * @param docUrl  本机服务器端口号
     */
    public ConfigClient(String appName, String host, int port, String docUrl) {
        this(appName, host, port);
        this.docUrl = docUrl;
    }

    public void init() {
        this.initProcessor();
        this.addInitializer(this.permissionManager);
        this.addInitializer(this.appSecretManager);

        Config config = Context.getConfig();

        config.setAppSecretManager(this.appSecretManager);
        config.setPermissionManager(this.permissionManager);

        // 启动netty客户端
        final CountDownLatch latch = CountDownLatchManager.initCountDownLatch(AbstractNettyProcessor.getLockObjectCount());
        ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            // 捕获线程中的异常
            t.setUncaughtExceptionHandler((t1, e) -> {
                Logger.error("Netty客户端启动失败", e);
                System.exit(0);
            });
            return t;
        });
        executorService.execute(() -> new NettyClient(ConfigClient.this, host, port).run());
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logger.error("latch cased InterruptedException", e);
            Thread.currentThread().interrupt();
        }
    }

    public void initProcessor() {
        this.addProcessor(new DownloadPermissionConfigProcessor(this, NettyMode.DOWNLOAD_PERMISSION_CONFIG));
        this.addProcessor(new DownloadSecretConfigProcessor(this, NettyMode.DOWNLOAD_SECRET_CONFIG));
        this.addProcessor(new UpdatePermissionConfigProcessor(this, NettyMode.UPDATE_PERMISSION_CONFIG));
        this.addProcessor(new UpdateSecretConfigProcessor(this, NettyMode.UPDATE_SECRET_CONFIG));
    }

    private void addProcessor(AbstractNettyProcessor nettyProcessor) {
        processorMap.put(nettyProcessor.getCode(), nettyProcessor);
    }


    public void addInitializer(ManagerInitializer initializer) {
        if (initializer != null) {
            this.initializers.add(initializer);
        }
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public LocalAppSecretManager getAppSecretManager() {
        return appSecretManager;
    }

    public Map<String, NettyProcessor> getProcessorMap() {
        return processorMap;
    }

    public List<ManagerInitializer> getInitializers() {
        return initializers;
    }

    public String getLocalServerPort() {
        return docUrl;
    }

}
