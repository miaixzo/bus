/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.metric.net;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.miaixz.bus.image.Device;

/**
 * Represents the DeviceService type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DeviceService implements DeviceServiceInterface {

    /**
     * The device value.
     */
    protected Device device;

    /**
     * The executor value.
     */
    protected ExecutorService executor;

    /**
     * The scheduled executor value.
     */
    protected ScheduledExecutorService scheduledExecutor;

    /**
     * Executes the init operation.
     *
     * @param device the device.
     */
    protected void init(Device device) {
        setDevice(device);
    }

    /**
     * Gets the device.
     *
     * @return the device.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Sets the device.
     *
     * @param device the device.
     */
    public void setDevice(Device device) {
        this.device = device;
    }

    /**
     * Determines whether running.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isRunning() {
        return executor != null;
    }

    /**
     * Executes the start operation.
     *
     * @throws Exception if the operation cannot be completed.
     */
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

    /**
     * Executes the stop operation.
     */
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

    /**
     * Executes the executer service operation.
     *
     * @return the operation result.
     */
    protected ExecutorService executerService() {
        return Executors.newCachedThreadPool();
    }

    /**
     * Executes the scheduled executer service operation.
     *
     * @return the operation result.
     */
    protected ScheduledExecutorService scheduledExecuterService() {
        return Executors.newSingleThreadScheduledExecutor();
    }

}
