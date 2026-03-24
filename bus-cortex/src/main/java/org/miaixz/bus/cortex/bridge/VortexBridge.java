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
package org.miaixz.bus.cortex.bridge;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.registry.api.ApiDefinition;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.logger.Logger;

/**
 * Asynchronous gateway sync bridge that forwards registry events to vortex.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class VortexBridge {

    /**
     * Base gateway URL used for registry sync callbacks.
     */
    private final String syncUrl;
    /**
     * Maximum number of delivery retries for a single sync event.
     */
    private final int maxRetries;
    /**
     * Bounded queue holding pending registry sync events.
     */
    private final LinkedBlockingQueue<SyncEvent> queue = new LinkedBlockingQueue<>(10000);
    /**
     * Whether the background worker should keep processing events.
     */
    private volatile boolean running = true;
    /**
     * Background worker thread responsible for delivering sync events.
     */
    private final Thread workerThread;

    /**
     * Creates a VortexBridge and starts the background sync worker.
     *
     * @param syncUrl    base URL of the gateway's sync endpoint
     * @param maxRetries maximum delivery attempts per event before dropping
     */
    public VortexBridge(String syncUrl, int maxRetries) {
        this.syncUrl = syncUrl;
        this.maxRetries = maxRetries;
        this.workerThread = new Thread(this::run, "cortex-vortex-bridge");
        this.workerThread.setDaemon(true);
        this.workerThread.start();
    }

    /**
     * Enqueues a REGISTER sync event for the given service and instance.
     *
     * @param service  registered service
     * @param instance registered instance
     */
    public void onRegistered(ApiDefinition service, Instance instance) {
        Assets asset = ApiAssetsConverter.convert(service, instance);
        SyncEvent event = new SyncEvent();
        event.setAction(SyncEvent.SyncAction.REGISTER);
        event.setAsset(asset);
        event.setNamespace(service.getNamespace());
        event.setSequence(System.currentTimeMillis());
        if (!queue.offer(event)) {
            Logger.warn("VortexBridge queue full, dropping REGISTER event for: {}", service.getName());
        }
    }

    /**
     * Enqueues a DEREGISTER sync event.
     *
     * @param namespace service namespace
     * @param method    service method
     * @param version   service version
     */
    public void onDeregistered(String namespace, String method, String version) {
        SyncEvent event = new SyncEvent();
        event.setAction(SyncEvent.SyncAction.DEREGISTER);
        event.setNamespace(namespace);
        event.setSequence(System.currentTimeMillis());
        if (!queue.offer(event)) {
            Logger.warn("VortexBridge queue full, dropping DEREGISTER event for: {}/{}", method, version);
        }
    }

    /**
     * Stops the background sync worker.
     */
    public void stop() {
        running = false;
        workerThread.interrupt();
    }

    /**
     * Drains queued sync events and posts them to the vortex internal sync endpoint.
     */
    private void run() {
        while (running) {
            try {
                SyncEvent event = queue.poll(1, TimeUnit.SECONDS);
                if (event == null) {
                    continue;
                }
                String json = JsonKit.toJsonString(event);
                String url = syncUrl + Builder.INTERNAL_SYNC_PATH;
                int attempt = 0;
                while (attempt < maxRetries) {
                    try {
                        Httpx.post(url, json, "application/json");
                        break;
                    } catch (Exception e) {
                        attempt++;
                        if (attempt >= maxRetries) {
                            Logger.warn("VortexBridge failed to sync after {} retries: {}", maxRetries, e.getMessage());
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                Logger.warn("VortexBridge worker error: {}", e.getMessage());
            }
        }
    }

}
