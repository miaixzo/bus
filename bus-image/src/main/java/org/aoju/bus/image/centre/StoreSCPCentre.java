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
package org.aoju.bus.image.centre;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.image.metric.Connection;
import org.aoju.bus.image.metric.TransferCapability;
import org.aoju.bus.image.plugin.StoreSCP;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */
public class StoreSCPCentre extends AbstractCentre {

    protected StoreSCP storeSCP;
    protected ExecutorService executor;
    protected ScheduledExecutorService scheduledExecutor;

    public static StoreSCPCentre Builder() {
        return new StoreSCPCentre();
    }

    @Override
    public boolean isRunning() {
        return storeSCP.getConnection().isListening();
    }

    @Override
    public synchronized void start() {
        if (isRunning()) {
            throw new InstrumentException("Cannot start a Listener because it is already running.");
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
        if (transferCapabilityFile != null) {
            storeSCP.loadDefaultTransferCapability(transferCapabilityFile);
        } else {
            storeSCP.getApplicationEntity()
                    .addTransferCapability(new TransferCapability(null, Symbol.STAR, TransferCapability.Role.SCP, Symbol.STAR));
        }

        device.start();
    }

    @Override
    public synchronized void stop() {
        if (device != null) {
            device.stop();
        }
        if (executor != null) {
            executor.shutdown();
            scheduledExecutor = null;
        }
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
            executor = null;
        }
    }

    @Override
    public StoreSCPCentre build() {
        if (executor != null) {
            throw new IllegalStateException("Already started");
        }
        if (ObjectUtils.isEmpty(node)) {
            throw new NullPointerException("The node cannot be null.");
        }
        if (ObjectUtils.isEmpty(args)) {
            throw new NullPointerException("The args cannot be null.");
        }
        if (ObjectUtils.isEmpty(device)) {
            throw new NullPointerException("The device cannot be null.");
        }
        if (rollers != null) {
            storeSCP.setRollers(rollers);
        }
        executor = executerService();
        scheduledExecutor = scheduledExecuterService();

        device.setExecutor(executor);
        device.setScheduledExecutor(scheduledExecutor);
        return this;
    }

    public StoreSCP getStoreSCP() {
        return storeSCP;
    }

    public void setStoreSCP(String path) {
        this.storeSCP = new StoreSCP(path);
    }

    protected ExecutorService executerService() {
        return Executors.newCachedThreadPool();
    }

    protected ScheduledExecutorService scheduledExecuterService() {
        return Executors.newSingleThreadScheduledExecutor();
    }

}
