/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.image.metric.net;

import org.miaixz.bus.image.Device;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class DeviceService implements DeviceServiceInterface {

    protected Device device;
    protected ExecutorService executor;
    protected ScheduledExecutorService scheduledExecutor;

    protected void init(Device device) {
        setDevice(device);
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public boolean isRunning() {
        return executor != null;
    }

    public void start() throws Exception {
        if (device == null)
            throw new IllegalStateException("Not initialized");
        if (executor != null)
            throw new IllegalStateException("Already started");
        executor = executerService();
        scheduledExecutor = scheduledExecuterService();
        try {
            device.setExecutor(executor);
            device.setScheduledExecutor(scheduledExecutor);
            device.bindConnections();
        } catch (Exception e) {
            stop();
            throw e;
        }
    }

    public void stop() {
        if (device != null)
            device.unbindConnections();
        if (scheduledExecutor != null)
            scheduledExecutor.shutdown();
        if (executor != null)
            executor.shutdown();
        executor = null;
        scheduledExecutor = null;
    }

    protected ExecutorService executerService() {
        return Executors.newCachedThreadPool();
    }

    protected ScheduledExecutorService scheduledExecuterService() {
        return Executors.newSingleThreadScheduledExecutor();
    }

}
