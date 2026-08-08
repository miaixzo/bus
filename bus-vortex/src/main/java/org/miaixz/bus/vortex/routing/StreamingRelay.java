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
package org.miaixz.bus.vortex.routing;

import java.time.Duration;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.PooledDataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.miaixz.bus.vortex.Delivery;
import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.Octets;
import org.miaixz.bus.vortex.guard.AdmissionGate;

import reactor.core.publisher.Flux;

/**
 * Applies admission, memory-pressure checks and cancellation-safe buffer handling to streaming responses.
 * <p>
 * Realtime streams and downloads use separate mode-specific limits under one shared streaming limit. Download mode
 * additionally enforces a no-progress timeout and a minimum average transfer rate after the configured grace period.
 * The relay never aggregates response data and therefore preserves downstream backpressure.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class StreamingRelay {

    /**
     * Restricts the class to static relay operations.
     */
    private StreamingRelay() {
        // No initialization required.
    }

    /**
     * Wraps a downstream body with the limits associated with its delivery.
     * <p>
     * The streaming lease remains owned until the returned publisher completes, fails or is cancelled. Discarded pooled
     * buffers are released when downstream cancellation or an operator prevents delivery.
     *
     * @param source   downstream response buffers
     * @param delivery realtime-stream or download delivery
     * @return protected, backpressure-aware response publisher
     */
    public static Flux<DataBuffer> relay(Flux<DataBuffer> source, Delivery delivery) {
        if (source == null) {
            return Flux.empty();
        }
        return Flux.using(() -> acquire(delivery), lease -> protect(source, delivery), AdmissionGate.Lease::close)
                .doOnDiscard(PooledDataBuffer.class, Octets::release);
    }

    /**
     * Adds download progress checks while preserving the source publisher for realtime streams.
     *
     * @param source   downstream response buffers
     * @param delivery realtime-stream or download delivery
     * @return source publisher with mode-specific progress protection
     */
    private static Flux<DataBuffer> protect(Flux<DataBuffer> source, Delivery delivery) {
        Flux<DataBuffer> protectedSource = source.doOnDiscard(PooledDataBuffer.class, Octets::release);
        if (delivery == Delivery.DOWNLOAD) {
            Duration progressTimeout = Duration.ofSeconds(Holder.get().getDownloadNoProgressTimeoutSeconds());
            return Flux.defer(() -> {
                long started = System.nanoTime();
                long[] bytes = new long[1];
                return protectedSource.<DataBuffer>handle((buffer, sink) -> {
                    bytes[0] += buffer.readableByteCount();
                    long elapsedNanos = System.nanoTime() - started;
                    if (elapsedNanos >= progressTimeout.toNanos()) {
                        long bytesPerSecond = (long) (bytes[0] / (elapsedNanos / 1_000_000_000.0d));
                        if (bytesPerSecond < Holder.get().getDownloadMinimumBytesPerSecond()) {
                            Octets.release(buffer);
                            sink.error(
                                    new java.util.concurrent.TimeoutException(
                                            "Download rate below configured minimum"));
                            return;
                        }
                    }
                    sink.next(buffer);
                });
            }).timeout(progressTimeout);
        }
        return protectedSource;
    }

    /**
     * Acquires the mode-specific streaming lease after checking sampled memory pressure.
     *
     * @param delivery realtime-stream or download delivery
     * @return lease containing both shared and mode-specific permits
     * @throws ResponseStatusException when memory pressure or admission capacity rejects the stream
     */
    private static AdmissionGate.Lease acquire(Delivery delivery) {
        boolean pressure = delivery == Delivery.DOWNLOAD ? Holder.memoryPressure().rejectDownloads()
                : Holder.memoryPressure().rejectStreaming();
        if (pressure) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Streaming paused by memory pressure");
        }
        AdmissionGate.Lease lease = delivery == Delivery.DOWNLOAD ? Holder.admissionGate().tryAcquireDownload()
                : Holder.admissionGate().tryAcquireRealtimeStream();
        if (lease == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Streaming capacity exhausted");
        }
        return lease;
    }
}
