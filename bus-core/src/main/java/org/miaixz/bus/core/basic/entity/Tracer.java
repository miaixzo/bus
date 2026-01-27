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
package org.miaixz.bus.core.basic.entity;

import java.io.Serial;

import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents tracing information for a request, extending authorization details. This class captures metadata related
 * to request tracing, such as unique identifiers, IP addresses, and client information, to facilitate tracking and
 * debugging of requests across distributed systems.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
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
