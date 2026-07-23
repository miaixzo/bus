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
package org.miaixz.bus.fabric.network.kcp;

import java.time.Duration;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Policy;

/**
 * Immutable KCP wire, flow-control, retransmission, and reassembly policy.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class KcpPolicy implements Policy {

    /**
     * Typed option for the complete KCP policy.
     */
    public static final Options.Key<KcpPolicy> OPTION = Options.key("kcp.policy", KcpPolicy.class);

    /**
     * Shared default policy.
     */
    private static final KcpPolicy DEFAULTS = builder().build();

    /**
     * KCP wire format version.
     */
    private final int wireVersion;

    /**
     * Maximum number of unacknowledged outbound packets.
     */
    private final int sendWindowSize;

    /**
     * Maximum number of inbound packets accepted by the receive window.
     */
    private final int receiveWindowSize;

    /**
     * Delay before an unacknowledged packet is retransmitted.
     */
    private final Duration retransmitDelay;

    /**
     * Precomputed retransmission delay in milliseconds for packet hot paths.
     */
    private final long retransmitDelayMillis;

    /**
     * Maximum retransmission attempts allowed for one packet.
     */
    private final int maxRetransmissions;

    /**
     * Maximum lifetime of an incomplete fragmented message.
     */
    private final Duration reassemblyTimeout;

    /**
     * Precomputed reassembly timeout in milliseconds for cleanup hot paths.
     */
    private final long reassemblyTimeoutMillis;

    /**
     * Maximum number of incomplete messages retained concurrently.
     */
    private final int maxActiveReassemblies;

    /**
     * Maximum decoded message size in bytes.
     */
    private final long maxMessageBytes;

    /**
     * Maximum queued outbound payload size in bytes.
     */
    private final long maxOutboundQueueBytes;

    /**
     * Maximum aggregate reassembly memory in bytes.
     */
    private final long maxReassemblyBytes;

    /**
     * Maximum reassembly memory in bytes assigned to one remote source.
     */
    private final long maxSourceReassemblyBytes;

    /**
     * Creates one validated policy and precomputes time values used by packet hot paths.
     *
     * @param builder policy values to validate and snapshot
     */
    private KcpPolicy(final Builder builder) {
        wireVersion = wireVersion(builder.wireVersion);
        sendWindowSize = window(builder.sendWindowSize, "KCP send window");
        receiveWindowSize = window(builder.receiveWindowSize, "KCP receive window");
        retransmitDelay = duration(builder.retransmitDelay, "KCP retransmit delay");
        retransmitDelayMillis = millis(retransmitDelay, "KCP retransmit delay");
        maxRetransmissions = positive(builder.maxRetransmissions, "KCP maximum retransmissions");
        reassemblyTimeout = duration(builder.reassemblyTimeout, "KCP reassembly timeout");
        reassemblyTimeoutMillis = millis(reassemblyTimeout, "KCP reassembly timeout");
        maxActiveReassemblies = positive(builder.maxActiveReassemblies, "KCP active reassembly limit");
        maxMessageBytes = positive(builder.maxMessageBytes, "KCP maximum message bytes");
        maxOutboundQueueBytes = positive(builder.maxOutboundQueueBytes, "KCP outbound queue bytes");
        maxReassemblyBytes = positive(builder.maxReassemblyBytes, "KCP reassembly bytes");
        maxSourceReassemblyBytes = positive(builder.maxSourceReassemblyBytes, "KCP source reassembly bytes");
        if (maxReassemblyBytes < maxMessageBytes || maxSourceReassemblyBytes < maxMessageBytes
                || maxSourceReassemblyBytes > maxReassemblyBytes) {
            throw new ValidateException("KCP reassembly limits are inconsistent");
        }
    }

    /**
     * Returns the shared default KCP policy.
     *
     * @return default KCP policy
     */
    public static KcpPolicy defaults() {
        return DEFAULTS;
    }

    /**
     * Creates a builder initialized with the default KCP values.
     *
     * @return new KCP policy builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Resolves the KCP policy from an option snapshot.
     *
     * @param options option source
     * @return configured policy or the shared default policy
     */
    public static KcpPolicy resolve(final Options options) {
        final Options current = Assert.notNull(options, () -> new ValidateException("Options must not be null"));
        final KcpPolicy configured = current.get(OPTION);
        return configured == null ? defaults() : configured;
    }

    /**
     * Adds this complete KCP policy to an immutable option snapshot.
     *
     * @param options option source
     * @return updated options containing this policy
     */
    @Override
    public Options from(final Options options) {
        return Assert.notNull(options, () -> new ValidateException("Options must not be null")).with(OPTION, this);
    }

    /**
     * Returns the KCP wire format version.
     *
     * @return wire version, either {@code 1} or {@code 2}
     */
    public int wireVersion() {
        return wireVersion;
    }

    /**
     * Returns the outbound packet window size.
     *
     * @return send window size
     */
    public int sendWindowSize() {
        return sendWindowSize;
    }

    /**
     * Returns the inbound packet window size.
     *
     * @return receive window size
     */
    public int receiveWindowSize() {
        return receiveWindowSize;
    }

    /**
     * Returns the retransmission delay.
     *
     * @return non-negative retransmission delay
     */
    public Duration retransmitDelay() {
        return retransmitDelay;
    }

    /**
     * Returns the precomputed retransmission delay.
     *
     * @return retransmission delay in milliseconds
     */
    public long retransmitDelayMillis() {
        return retransmitDelayMillis;
    }

    /**
     * Returns the retransmission attempt limit.
     *
     * @return positive maximum retransmission count
     */
    public int maxRetransmissions() {
        return maxRetransmissions;
    }

    /**
     * Returns the incomplete-message lifetime.
     *
     * @return non-negative reassembly timeout
     */
    public Duration reassemblyTimeout() {
        return reassemblyTimeout;
    }

    /**
     * Returns the precomputed incomplete-message lifetime.
     *
     * @return reassembly timeout in milliseconds
     */
    public long reassemblyTimeoutMillis() {
        return reassemblyTimeoutMillis;
    }

    /**
     * Returns the concurrent incomplete-message limit.
     *
     * @return positive active reassembly limit
     */
    public int maxActiveReassemblies() {
        return maxActiveReassemblies;
    }

    /**
     * Returns the decoded message size limit.
     *
     * @return positive maximum message size in bytes
     */
    public long maxMessageBytes() {
        return maxMessageBytes;
    }

    /**
     * Returns the outbound queue size limit.
     *
     * @return positive maximum queued payload size in bytes
     */
    public long maxOutboundQueueBytes() {
        return maxOutboundQueueBytes;
    }

    /**
     * Returns the aggregate reassembly memory limit.
     *
     * @return positive aggregate reassembly limit in bytes
     */
    public long maxReassemblyBytes() {
        return maxReassemblyBytes;
    }

    /**
     * Returns the per-source reassembly memory limit.
     *
     * @return positive per-source reassembly limit in bytes
     */
    public long maxSourceReassemblyBytes() {
        return maxSourceReassemblyBytes;
    }

    /**
     * Validates the supported wire format version.
     *
     * @param value wire version candidate
     * @return validated wire version
     */
    private static int wireVersion(final int value) {
        if (value != Normal._1 && value != Normal._2) {
            throw new ValidateException("KCP wire version must be 1 or 2");
        }
        return value;
    }

    /**
     * Validates a KCP packet window size.
     *
     * @param value window size candidate
     * @param name  logical field name used in validation messages
     * @return validated window size
     */
    private static int window(final int value, final String name) {
        return Assert.checkBetween(
                value,
                Normal._1,
                Normal._65535,
                () -> new ValidateException(name + " must be between 1 and 65535"));
    }

    /**
     * Validates a positive integer limit.
     *
     * @param value limit candidate
     * @param name  logical field name used in validation messages
     * @return validated positive limit
     */
    private static int positive(final int value, final String name) {
        if (value <= Normal._0) {
            throw new ValidateException(name + " must be positive");
        }
        return value;
    }

    /**
     * Validates a positive long limit.
     *
     * @param value limit candidate
     * @param name  logical field name used in validation messages
     * @return validated positive limit
     */
    private static long positive(final long value, final String name) {
        if (value <= Normal.LONG_ZERO) {
            throw new ValidateException(name + " must be positive");
        }
        return value;
    }

    /**
     * Validates a non-negative duration.
     *
     * @param value duration candidate
     * @param name  logical field name used in validation messages
     * @return validated duration
     */
    private static Duration duration(final Duration value, final String name) {
        final Duration checked = Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
        if (checked.isNegative()) {
            throw new ValidateException(name + " must not be negative");
        }
        return checked;
    }

    /**
     * Converts a duration to its exact millisecond representation.
     *
     * @param value validated duration
     * @param name  logical field name used in validation messages
     * @return duration in milliseconds
     */
    private static long millis(final Duration value, final String name) {
        try {
            return value.toMillis();
        } catch (final ArithmeticException e) {
            throw new ValidateException(name + " is too large", e);
        }
    }

    /**
     * Builder for immutable KCP policies.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Wire format version candidate.
         */
        private int wireVersion = Normal._1;

        /**
         * Send window size candidate.
         */
        private int sendWindowSize = Normal._32;

        /**
         * Receive window size candidate.
         */
        private int receiveWindowSize = Normal._32;

        /**
         * Retransmission delay candidate.
         */
        private Duration retransmitDelay = org.miaixz.bus.fabric.Builder.KCP_NETWORK_DEFAULT_RETRANSMIT_DELAY;

        /**
         * Retransmission attempt limit candidate.
         */
        private int maxRetransmissions = org.miaixz.bus.fabric.Builder.KCP_NETWORK_MAX_RETRANSMISSIONS;

        /**
         * Reassembly timeout candidate.
         */
        private Duration reassemblyTimeout = org.miaixz.bus.fabric.Builder.KCP_NETWORK_REASSEMBLY_TIMEOUT;

        /**
         * Concurrent reassembly limit candidate.
         */
        private int maxActiveReassemblies = org.miaixz.bus.fabric.Builder.KCP_NETWORK_MAX_ACTIVE_REASSEMBLIES;

        /**
         * Decoded message size limit candidate.
         */
        private long maxMessageBytes = org.miaixz.bus.fabric.Builder.KCP_NETWORK_MAX_MESSAGE_BYTES;

        /**
         * Outbound queue size limit candidate.
         */
        private long maxOutboundQueueBytes = org.miaixz.bus.fabric.Builder.KCP_NETWORK_MAX_OUTBOUND_QUEUE_BYTES;

        /**
         * Aggregate reassembly memory limit candidate.
         */
        private long maxReassemblyBytes = org.miaixz.bus.fabric.Builder.KCP_NETWORK_MAX_REASSEMBLY_BYTES;

        /**
         * Per-source reassembly memory limit candidate.
         */
        private long maxSourceReassemblyBytes = org.miaixz.bus.fabric.Builder.KCP_NETWORK_MAX_SOURCE_REASSEMBLY_BYTES;

        /**
         * Creates a builder initialized with default KCP values.
         */
        private Builder() {
            // Field initializers provide the complete default policy.
        }

        /**
         * Sets the KCP wire format version.
         *
         * @param value wire version, either {@code 1} or {@code 2}
         * @return this builder
         */
        public Builder wireVersion(final int value) {
            wireVersion = value;
            return this;
        }

        /**
         * Sets the outbound packet window size.
         *
         * @param value window size from 1 through 65535
         * @return this builder
         */
        public Builder sendWindowSize(final int value) {
            sendWindowSize = value;
            return this;
        }

        /**
         * Sets the inbound packet window size.
         *
         * @param value window size from 1 through 65535
         * @return this builder
         */
        public Builder receiveWindowSize(final int value) {
            receiveWindowSize = value;
            return this;
        }

        /**
         * Sets the retransmission delay.
         *
         * @param value non-negative retransmission delay
         * @return this builder
         */
        public Builder retransmitDelay(final Duration value) {
            retransmitDelay = value;
            return this;
        }

        /**
         * Sets the retransmission attempt limit.
         *
         * @param value positive maximum retransmission count
         * @return this builder
         */
        public Builder maxRetransmissions(final int value) {
            maxRetransmissions = value;
            return this;
        }

        /**
         * Sets the incomplete-message lifetime.
         *
         * @param value non-negative reassembly timeout
         * @return this builder
         */
        public Builder reassemblyTimeout(final Duration value) {
            reassemblyTimeout = value;
            return this;
        }

        /**
         * Sets the concurrent incomplete-message limit.
         *
         * @param value positive active reassembly limit
         * @return this builder
         */
        public Builder maxActiveReassemblies(final int value) {
            maxActiveReassemblies = value;
            return this;
        }

        /**
         * Sets the decoded message size limit.
         *
         * @param value positive maximum message size in bytes
         * @return this builder
         */
        public Builder maxMessageBytes(final long value) {
            maxMessageBytes = value;
            return this;
        }

        /**
         * Sets the outbound queue size limit.
         *
         * @param value positive maximum queued payload size in bytes
         * @return this builder
         */
        public Builder maxOutboundQueueBytes(final long value) {
            maxOutboundQueueBytes = value;
            return this;
        }

        /**
         * Sets the aggregate reassembly memory limit.
         *
         * @param value positive aggregate reassembly limit in bytes
         * @return this builder
         */
        public Builder maxReassemblyBytes(final long value) {
            maxReassemblyBytes = value;
            return this;
        }

        /**
         * Sets the per-source reassembly memory limit.
         *
         * @param value positive per-source reassembly limit in bytes
         * @return this builder
         */
        public Builder maxSourceReassemblyBytes(final long value) {
            maxSourceReassemblyBytes = value;
            return this;
        }

        /**
         * Builds and validates an immutable KCP policy.
         *
         * @return immutable KCP policy containing the current builder values
         */
        public KcpPolicy build() {
            return new KcpPolicy(this);
        }

    }

}
