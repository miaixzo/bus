/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.vortex.provider;

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.vortex.Assets;
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
