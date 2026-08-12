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
package org.miaixz.bus.auth.metric.radius.eap;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Thread-safe ordered, expiring, single-completion EAP-TLS fragment reassembly state.
 */
public final class EapTlsFragmentState {

    /**
     * Maximum reassembled bytes.
     */
    private final int maximumBytes;

    /**
     * Absolute state deadline.
     */
    private final Instant deadline;

    /**
     * Reassembly buffer.
     */
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    /**
     * Next required sequence.
     */
    private int sequence;

    /**
     * Terminal completion flag.
     */
    private boolean complete;

    /**
     * Creates one state.
     *
     * @param maximumBytes maximum bytes
     * @param created      creation time
     * @param ttl          positive TTL
     */
    public EapTlsFragmentState(final int maximumBytes, final Instant created, final Duration ttl) {
        Assert.isTrue(
                maximumBytes > Normal._0 && maximumBytes <= 64 * Normal._1024,
                () -> new ValidateException("EAP-TLS fragment limit is invalid"));
        this.maximumBytes = maximumBytes;
        final Instant start = Assert
                .notNull(created, () -> new ValidateException("EAP-TLS creation time must not be null"));
        final Duration lifetime = Assert.notNull(ttl, () -> new ValidateException("EAP-TLS TTL must not be null"));
        Assert.isTrue(
                !lifetime.isNegative() && !lifetime.isZero() && lifetime.compareTo(Duration.ofSeconds(30)) <= 0,
                () -> new ValidateException("EAP-TLS TTL is invalid"));
        this.deadline = start.plus(lifetime);
    }

    /**
     * Accepts one exact ordered fragment and returns data only at terminal completion.
     *
     * @param receivedSequence received sequence
     * @param more             whether more fragments follow
     * @param fragment         fragment bytes
     * @param now              current security time
     * @return completed payload or empty
     */
    public synchronized Optional<byte[]> accept(
            final int receivedSequence,
            final boolean more,
            final byte[] fragment,
            final Instant now) {
        Assert.isTrue(!complete, () -> new ValidateException("EAP-TLS fragment state is already complete"));
        Assert.isTrue(
                !Assert.notNull(now, () -> new ValidateException("EAP-TLS current time must not be null"))
                        .isAfter(deadline),
                () -> new ValidateException("EAP-TLS fragment state expired"));
        Assert.isTrue(
                receivedSequence == sequence,
                () -> new ValidateException("EAP-TLS fragment sequence is invalid"));
        final byte[] value = Arrays.copyOf(
                Assert.notNull(fragment, () -> new ValidateException("EAP-TLS fragment must not be null")),
                fragment.length);
        Assert.isTrue(
                buffer.size() + value.length <= maximumBytes,
                () -> new ValidateException("EAP-TLS fragments exceed their byte limit"));
        buffer.writeBytes(value);
        sequence++;
        if (more) {
            return Optional.empty();
        }
        complete = true;
        return Optional.of(buffer.toByteArray());
    }

    /**
     * Returns whether terminal data was emitted.
     *
     * @return completion state
     */
    public synchronized boolean complete() {
        return complete;
    }

}
