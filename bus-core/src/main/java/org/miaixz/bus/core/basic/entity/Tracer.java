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
package org.miaixz.bus.core.basic.entity;

import java.io.Serial;

import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents tracing information for a request, extending authorization details. This class captures metadata related
 * to request tracing, such as unique identifiers, IP addresses, and client information, to facilitate tracking and
 * debugging of requests across distributed systems.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
public class Tracer extends Authorize {

    @Serial
    private static final long serialVersionUID = 2852291120377L;

    /**
     * Unique identifier for the current request. This ID is used to track a specific request within the system.
     */
    @Transient
    protected String x_request_id;

    /**
     * Main trace ID for the entire request chain. This ID links all related requests across services in a distributed
     * trace.
     */
    @Transient
    protected String x_trace_id;

    /**
     * Identifier for the calling service or component. Represents the span of the current service in the request chain.
     */
    @Transient
    protected String x_span_id;

    /**
     * Identifier for the service or component being called. Tracks the child span in the request chain for hierarchical
     * tracing.
     */
    @Transient
    protected String x_child_id;

    /**
     * Remote IPv4 address of the client making the request. Identifies the client's network origin.
     */
    @Transient
    protected String x_request_ip;

    /**
     * Local IPv6 address of the service handling the request. Used to identify the server processing the request.
     */
    @Transient
    protected String x_request_ipv6;

    /**
     * Remote IPv4 address of the client making the request. Identifies the client's network origin.
     */
    @Transient
    protected String x_request_ipv4;

    /**
     * Domain name associated with the client's request. Provides additional context about the client's origin.
     */
    @Transient
    protected String x_request_domain;

    /**
     * Channel type of the requester. Indicates the platform used to make the request, such as: 1 - Web, 2 - Mobile App,
     * 3 - DingTalk, 4 - WeChat Mini Program, 5 - Other.
     */
    @Transient
    protected String x_request_channel;

    /**
     * Terminal type of the requester. Specifies the device type used by the client, such as: 1 - PC, 2 - Android, 3 -
     * iPhone, 4 - iPad, 5 - Windows Phone, 6 - HarmonyOS, 7 - Other.
     */
    @Transient
    protected String x_request_terminal;

    /**
     * Constructs an empty tracer entity.
     */
    public Tracer() {
    }

    /**
     * Gets the client IP address, preferring IPv4 over IPv6.
     * <p>
     * This method returns the IPv4 address if available. If IPv4 is not available or empty, it falls back to IPv6. If
     * neither is available, it returns the legacy x_request_ip field.
     *
     * @return The client IP address (IPv4 preferred, IPv6 as fallback, or legacy IP)
     */
    public String getX_request_ip() {
        // Priority 1: IPv4
        if (x_request_ipv4 != null && !x_request_ipv4.isEmpty()) {
            return x_request_ipv4;
        }
        // Priority 2: IPv6
        if (x_request_ipv6 != null && !x_request_ipv6.isEmpty()) {
            return x_request_ipv6;
        }
        // Priority 3: Legacy x_request_ip
        return x_request_ip;
    }

}
