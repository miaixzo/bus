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
package org.miaixz.bus.fabric.network.dns.forward;

/**
 * Transport used when forwarding DNS queries to an upstream server.
 *
 * @author Kimi Liu
 */
public enum DnsUpstreamTransport {

    /**
     * DNS over UDP upstream transport.
     */
    UDP,

    /**
     * DNS over TCP upstream transport with a two-byte message length prefix.
     */
    TCP,

    /**
     * DNS-over-TLS upstream transport with a two-byte message length prefix.
     */
    DOT,

    /**
     * DNS-over-HTTPS upstream transport carrying wire messages over HTTP.
     */
    DOH,

    /**
     * DNS-over-QUIC upstream transport carrying one DNS message per QUIC stream.
     */
    DOQ

}
