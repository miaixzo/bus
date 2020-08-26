/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.goalie.builtin;

import lombok.Data;
import org.aoju.bus.goalie.ApiConfig;
import org.aoju.bus.goalie.ApiContext;
import org.aoju.bus.goalie.consts.NettyMode;
import org.aoju.bus.goalie.manual.ManagerInitializer;
import org.aoju.bus.goalie.secure.ApiPermissionManager;
import org.aoju.bus.goalie.secure.LocalAppSecretManager;
import org.aoju.bus.goalie.secure.PermissionManager;
import org.aoju.bus.logger.Logger;

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
 * @version 6.0.8
 * @since JDK 1.8++
 */
@Data
public class ConfigClient {

    private Map<String, NettyClientProcessor> processorMap = new HashMap<>();

    private List<ManagerInitializer> initializers = new ArrayList<>(4);

    private PermissionManager permissionManager = new ApiPermissionManager();
    private LocalAppSecretManager appSecretManager = new LocalAppSecretManager();

    private String host;
    private int port;
    private String docUrl;


    /**
     * @param host 配置中心ip
     * @param port 配置中心端口
     */
    public ConfigClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * @param host   配置中心ip
     * @param port   配置中心端口
     * @param docUrl 本机服务器端口号
     */
    public ConfigClient(String host, int port, String docUrl) {
        this(host, port);
        this.docUrl = docUrl;
    }

    public void init() {
        this.initProcessor();
        this.addInitializer(this.permissionManager);
        this.addInitializer(this.appSecretManager);

        ApiConfig config = ApiContext.getConfig();

        config.setAppSecretManager(this.appSecretManager);
        config.setPermissionManager(this.permissionManager);

        // 启动netty客户端
        final CountDownLatch latch = CountDownLatchManager.initCountDownLatch(AbstractClientClientProcessor.getLockObjectCount());
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
        this.addProcessor(new DownloadLocalPermission(this, NettyMode.DOWNLOAD_PERMISSION_CONFIG));
        this.addProcessor(new DownloadLocalSecret(this, NettyMode.DOWNLOAD_SECRET_CONFIG));
        this.addProcessor(new UpdateLocalPermission(this, NettyMode.UPDATE_PERMISSION_CONFIG));
        this.addProcessor(new UpdateLocalSecret(this, NettyMode.UPDATE_SECRET_CONFIG));
    }

    private void addProcessor(AbstractClientClientProcessor nettyProcessor) {
        processorMap.put(nettyProcessor.getCode(), nettyProcessor);
    }


    public void addInitializer(ManagerInitializer initializer) {
        if (initializer != null) {
            this.initializers.add(initializer);
        }
    }

}
