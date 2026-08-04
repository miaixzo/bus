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
package org.miaixz.bus.fabric.network.dns.observe;

import java.net.InetAddress;
import java.time.Instant;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.message.DnsClientSubnet;
import org.miaixz.bus.fabric.network.dns.message.DnsQuery;
import org.miaixz.bus.fabric.network.dns.message.DnsResponseCode;
import org.miaixz.bus.fabric.network.dns.server.DnsTransport;
import org.miaixz.bus.logger.Logger;

/**
 * Optional DNS query logger for DNS Server requests.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsQueryLog {

    /**
     * Logger group used by DNS Server query log entries.
     */
    private static final String GROUP = "Fabric";

    /**
     * Text used when a value is absent.
     */
    private static final String NONE = "none";

    /**
     * Disabled query logger singleton.
     */
    private static final DnsQueryLog DISABLED = new DnsQueryLog(false);

    /**
     * Whether query logging is enabled.
     */
    private final boolean enabled;

    /**
     * Creates a query logger.
     *
     * @param enabled whether query logging is enabled
     */
    private DnsQueryLog(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the disabled query logger.
     *
     * @return disabled query logger
     */
    public static DnsQueryLog disabled() {
        return DISABLED;
    }

    /**
     * Creates an enabled query logger.
     *
     * @return enabled query logger
     */
    public static DnsQueryLog create() {
        return new DnsQueryLog(true);
    }

    /**
     * Returns whether query logging is enabled.
     *
     * @return true when logging is enabled
     */
    public boolean enabled() {
        return enabled;
    }

    /**
     * Writes one DNS query log entry.
     *
     * @param timestamp     wall-clock timestamp
     * @param clientAddress client address, or {@code null} when unavailable
     * @param endpoint      listener transport
     * @param view          selected view name
     * @param query         decoded query
     * @param responseCode  response code
     * @param latencyNanos  non-negative query latency in nanoseconds
     * @param policyAction  matched policy action, or {@code none}
     * @param upstream      selected upstream summary, or {@code local}
     */
    public void log(
            final Instant timestamp,
            final InetAddress clientAddress,
            final DnsTransport endpoint,
            final String view,
            final DnsQuery query,
            final DnsResponseCode responseCode,
            final long latencyNanos,
            final String policyAction,
            final String upstream) {
        if (!enabled || !Logger.isInfoEnabled()) {
            return;
        }
        if (timestamp == null) {
            throw new ValidateException("DNS query log timestamp must not be null");
        }
        if (endpoint == null) {
            throw new ValidateException("DNS query log endpoint must not be null");
        }
        if (view == null || view.isBlank()) {
            throw new ValidateException("DNS query log view must not be blank");
        }
        if (query == null) {
            throw new ValidateException("DNS query log query must not be null");
        }
        if (responseCode == null) {
            throw new ValidateException("DNS query log response code must not be null");
        }
        if (latencyNanos < 0L) {
            throw new ValidateException("DNS query log latency must be non-negative");
        }
        Logger.info(
                false,
                GROUP,
                "DNS query: timestamp={}, client={}, endpoint={}, view={}, qname={}, qtype={}, rcode={}, latencyNanos={}, policyAction={}, upstream={}, ecs={}",
                timestamp,
                address(clientAddress),
                endpoint.name(),
                view,
                query.question().name(),
                query.question().typeCode(),
                responseCode.name(),
                latencyNanos,
                value(policyAction),
                value(upstream),
                ecs(query.clientSubnet()));
    }

    /**
     * Returns a safe address token.
     *
     * @param address client address, or {@code null}
     * @return address token
     */
    private static String address(final InetAddress address) {
        return address == null ? NONE : address.getHostAddress();
    }

    /**
     * Returns a non-blank value or the absent token.
     *
     * @param value candidate value
     * @return safe value token
     */
    private static String value(final String value) {
        return value == null || value.isBlank() ? NONE : value;
    }

    /**
     * Returns a redacted ECS token.
     *
     * @param subnet EDNS Client Subnet value, or {@code null}
     * @return redacted ECS token without the client subnet address
     */
    private static String ecs(final DnsClientSubnet subnet) {
        if (subnet == null) {
            return NONE;
        }
        return "redacted/family-" + subnet.family() + "/source-" + subnet.sourcePrefixLength() + "/scope-"
                + subnet.scopePrefixLength();
    }

}
