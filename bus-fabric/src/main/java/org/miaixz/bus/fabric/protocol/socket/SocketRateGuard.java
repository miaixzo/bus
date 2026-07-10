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
package org.miaixz.bus.fabric.protocol.socket;

import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.guard.GuardResult;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.guard.frame.RateGuard;

/**
 * Socket rate guard backed by the current frame rate guard.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SocketRateGuard implements GuardRule {

    /**
     * Delegate byte-rate guard.
     */
    private final RateGuard delegate;

    /**
     * Creates a socket guard by delegating byte accounting to the frame-level rate guard.
     *
     * @param delegate frame-level byte budget guard
     */
    private SocketRateGuard(final RateGuard delegate) {
        this.delegate = delegate;
    }

    /**
     * Creates a socket rate guard.
     *
     * @param bytesPerSecond allowed bytes per second
     * @return socket rate guard
     */
    public static SocketRateGuard of(final long bytesPerSecond) {
        return new SocketRateGuard(RateGuard.of(bytesPerSecond));
    }

    /**
     * Acquires byte budget directly.
     *
     * @param bytes bytes to acquire
     * @return guard result
     */
    public GuardResult acquire(final long bytes) {
        return delegate.acquire(bytes);
    }

    /**
     * Returns currently available byte budget.
     *
     * @return available bytes
     */
    public long available() {
        return delegate.available();
    }

    @Override
    public GuardResult check(final Message message) {
        return delegate.check(message);
    }

    @Override
    public String name() {
        return "socket-rate";
    }

}
