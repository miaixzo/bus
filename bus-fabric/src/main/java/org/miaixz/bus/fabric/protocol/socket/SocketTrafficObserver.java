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

import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.observe.tags.Tags;

/**
 * Socket traffic observer backed by current observation markers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SocketTrafficObserver implements EventObserver {

    /**
     * Downstream observer.
     */
    private final EventObserver delegate;

    /**
     * Total observed socket read bytes.
     */
    private final AtomicLong readBytes;

    /**
     * Total observed socket write bytes.
     */
    private final AtomicLong writtenBytes;

    /**
     * Total observed socket failures.
     */
    private final AtomicLong failures;

    /**
     * Creates an observer that maintains socket counters before forwarding events downstream.
     *
     * @param delegate downstream observer
     */
    private SocketTrafficObserver(final EventObserver delegate) {
        this.delegate = EventObserver.safe(delegate);
        this.readBytes = new AtomicLong();
        this.writtenBytes = new AtomicLong();
        this.failures = new AtomicLong();
    }

    /**
     * Creates a socket traffic observer with no downstream observer.
     *
     * @return socket traffic observer
     */
    public static SocketTrafficObserver create() {
        return new SocketTrafficObserver(EventObserver.noop());
    }

    /**
     * Creates a socket traffic observer that forwards events to a downstream observer.
     *
     * @param delegate downstream observer
     * @return socket traffic observer
     */
    public static SocketTrafficObserver create(final EventObserver delegate) {
        return new SocketTrafficObserver(delegate);
    }

    @Override
    public void emit(final FabricEvent event) {
        if (event == null) {
            return;
        }
        final long bytes = bytes(event);
        if (event.marker() == ObservationMarker.SOCKET_READ) {
            readBytes.addAndGet(bytes);
        } else if (event.marker() == ObservationMarker.SOCKET_WRITE) {
            writtenBytes.addAndGet(bytes);
        } else if (event.marker() == ObservationMarker.SOCKET_FAILED) {
            failures.incrementAndGet();
        }
        delegate.emit(event);
    }

    /**
     * Returns total observed socket read bytes.
     *
     * @return read bytes
     */
    public long readBytes() {
        return readBytes.get();
    }

    /**
     * Returns total observed socket write bytes.
     *
     * @return written bytes
     */
    public long writtenBytes() {
        return writtenBytes.get();
    }

    /**
     * Returns total observed socket failures.
     *
     * @return failure count
     */
    public long failures() {
        return failures.get();
    }

    /**
     * Extracts the byte count tag from a socket event without failing the observer on malformed telemetry.
     *
     * @param event fabric event
     * @return byte count, or {@code 0} when absent or invalid
     */
    private static long bytes(final FabricEvent event) {
        final String value = event.tags().get(Tags.BYTES);
        if (StringKit.isBlank(value)) {
            return Normal.LONG_ZERO;
        }
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException ignored) {
            return Normal.LONG_ZERO;
        }
    }

}
