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
package org.miaixz.bus.image;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.BooleanKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.TransferCapability;
import org.miaixz.bus.image.plugin.StoreSCP;
import org.miaixz.bus.logger.Logger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Manages the lifecycle of a DICOM service process. This class acts as a central controller for starting and stopping
 * DICOM services, managing the main {@link Device} object, and coordinating its components like the {@link StoreSCP}.
 * It handles the initialization of executor services and the binding of network connections.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Centre {

    /**
     * The main DICOM device configuration, which encapsulates all network and application settings.
     */
    public Device device;

    /**
     * The Store SCP (Service Class Provider) implementation that defines the behavior for C-STORE operations.
     */
    public StoreSCP storeSCP;

    /**
     * The network node information (AET, hostname, port) used for binding the DICOM listener.
     */
    public Node node;

    /**
     * Configuration parameters, typically parsed from command-line arguments, for the DICOM service.
     */
    public Args args;

    /**
     * Custom post-processing logic to be applied after a DICOM object is stored.
     */
    public Efforts efforts;

    /**
     * The primary executor service for handling concurrent DICOM association tasks.
     */
    public ExecutorService executor;

    /**
     * The scheduled executor service for running delayed or periodic tasks.
     */
    public ScheduledExecutorService scheduledExecutor;

    /**
     * Constructs a new Centre with a specified device.
     *
     * @param device The DICOM device to manage. Must not be null.
     */
    public Centre(Device device) {
        this.device = Objects.requireNonNull(device);
    }

    /**
     * Gets the managed DICOM device.
     *
     * @return the device.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Checks if the service is currently running (i.e., if the executor service has been initialized).
     *
     * @return {@code true} if the service is running, {@code false} otherwise.
     */
    public boolean isRunning() {
        return executor != null;
    }

    /**
     * Starts the DICOM service with default settings.
     */
    public void start() {
        start(false);
    }

    /**
     * Starts the DICOM service manager. This method is synchronized to prevent concurrent startup. It can operate in
     * two modes depending on the flag: 1. If flag is true, it only initializes the executor services. 2. If flag is
     * false (default), it performs a full startup, configuring and binding the DICOM listener.
     *
     * @param flag A boolean flag to control the startup mode.
     * @throws InternalException    if the listener is already running.
     * @throws NullPointerException if essential components like StoreSCP, Node, or Args are not configured.
     */
    public synchronized void start(boolean... flag) {
        if (isRunning()) {
            return;
        }
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
            Logger.error("Error configuring TLS", e);
        }

        storeSCP.getApplicationEntity().setAcceptedCallingAETitles(args.getAcceptedCallingAETitles());

        URL sopClassesTCS = args.getSopClassesTCS();
        if (null != sopClassesTCS) {
            storeSCP.sopClassesTCS(sopClassesTCS);
        } else {
            storeSCP.getApplicationEntity().addTransferCapability(
                    new TransferCapability(null, Symbol.STAR, TransferCapability.Role.SCP, Symbol.STAR));
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
            Logger.error("Failed to start DICOM service: {}", e.getMessage(), e);
        }
    }

    /**
     * Stops the DICOM service manager. This method is synchronized. It unbinds all device connections and shuts down
     * the executor services.
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
