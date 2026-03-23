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
package org.miaixz.bus.vortex.provider;

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.cortex.Assets;
import reactor.core.publisher.Mono;

/**
 * A Service Provider Interface (SPI) for managing the lifecycle of external service processes.
 * <p>
 * Implementations of this interface are responsible for handling the operational logic of a service defined by its
 * {@link Assets}, such as starting, stopping, and checking its status. This abstraction allows the gateway to manage
 * services running as local processes, Docker containers, or on remote machines via different providers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ProcessProvider {

    /**
     * Starts the service defined by the given assets and returns a handle to the running process.
     * <p>
     * This method should be idempotent; calling it on an already running service should not cause an error and should
     * return a handle to the existing process.
     *
     * @param assets The configuration of the service to start.
     * @return A {@code Mono} emitting the {@link Process} handle upon successful start.
     */
    Mono<Process> start(Assets assets);

    /**
     * Stops the service defined by the given assets.
     * <p>
     * This method should be idempotent; calling it on an already stopped service should not cause an error.
     *
     * @param assets The configuration of the service to stop.
     * @return A {@code Mono<Void>} that completes when the stop signal has been successfully sent.
     */
    Mono<Void> stop(Assets assets);

    /**
     * Restarts the service defined by the given assets.
     * <p>
     * This is typically equivalent to calling {@code stop()} followed by {@code start()}.
     *
     * @param assets The configuration of the service to restart.
     * @return A {@code Mono} emitting the new {@link Process} handle upon successful restart.
     */
    Mono<Process> restart(Assets assets);

    /**
     * Retrieves the current status of the service.
     *
     * @param assets The configuration of the service to check.
     * @return A {@code Mono} emitting the current {@link EnumValue.Lifecycle}.
     */
    Mono<EnumValue.Lifecycle> getStatus(Assets assets);

}
