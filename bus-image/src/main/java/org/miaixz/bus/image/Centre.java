/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.image;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.toolkit.BooleanKit;
import org.miaixz.bus.core.toolkit.ObjectKit;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.TransferCapability;
import org.miaixz.bus.image.plugin.StoreSCP;
import org.miaixz.bus.logger.Logger;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 进程服务管理器
 * 1. 端口监听进程
 * 2. 设备服务进程
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@lombok.Builder
@NoArgsConstructor
@AllArgsConstructor
public class Centre {

    /**
     * 设备信息
     */
    public Device device;

    /**
     * 操作行为
     */
    public StoreSCP storeSCP;

    /**
     * 服务器信息
     */
    public Node node;
    /**
     * 参数信息
     */
    public Args args;

    /**
     * 业务处理
     */
    public Efforts efforts;
    /**
     * 任务处理者
     */
    public ExecutorService executor;
    /**
     * 业务处理
     */
    public ScheduledExecutorService scheduledExecutor;

    public Centre(Device device) {
        this.device = Objects.requireNonNull(device);
    }

    public void start() {
        start(false);
    }

    /**
     * 启动管理器
     *
     * @param flag 标记信息
     */
    public synchronized void start(boolean... flag) {
        if (BooleanKit.or(flag)) {
            if (null == executor) {
                executor = Executors.newSingleThreadExecutor();
                scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
                device.setExecutor(executor);
                device.setScheduledExecutor(scheduledExecutor);
            }
            return;
        }

        if (storeSCP.getConnection().isListening()) {
            throw new InternalException("Cannot start a Listener because it is already running.");
        }

        if (ObjectKit.isEmpty(storeSCP)) {
            throw new NullPointerException("The StoreSCP cannot be null.");
        }

        if (ObjectKit.isEmpty(node)) {
            throw new NullPointerException("The node cannot be null.");
        }
        if (ObjectKit.isEmpty(args)) {
            throw new NullPointerException("The args cannot be null.");
        }
        if (ObjectKit.isNotEmpty(efforts)) {
            storeSCP.setEfforts(efforts);
        }

        storeSCP.setStatus(0);

        Connection conn = storeSCP.getConnection();
        if (args.isBindCallingAet()) {
            args.configureBind(storeSCP.getApplicationEntity(), conn, node);
        } else {
            args.configureBind(conn, node);
        }

        args.configure(conn);
        try {
            args.configureTLS(conn, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        storeSCP.getApplicationEntity().setAcceptedCallingAETitles(args.getAcceptedCallingAETitles());

        URL transferCapabilityFile = args.getTransferCapabilityFile();
        if (null != transferCapabilityFile) {
            storeSCP.loadDefaultTransferCapability(transferCapabilityFile);
        } else {
            storeSCP.getApplicationEntity()
                    .addTransferCapability(new TransferCapability(null, Symbol.STAR, TransferCapability.Role.SCP, Symbol.STAR));
        }

        executor = Executors.newCachedThreadPool();
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        try {
            device = storeSCP.getDevice();
            device.setExecutor(executor);
            device.setScheduledExecutor(scheduledExecutor);
            device.bindConnections();
        } catch (IOException | GeneralSecurityException e) {
            stop();
            Logger.error(e.getMessage());
        }
    }

    /**
     * 停止管理器
     */
    public synchronized void stop() {
        if (null != device) {
            device.unbindConnections();
        }
        Builder.shutdown(scheduledExecutor);
        Builder.shutdown(executor);
        executor = null;
        scheduledExecutor = null;
    }

}
