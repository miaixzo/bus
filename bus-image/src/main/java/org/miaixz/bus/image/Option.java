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
package org.miaixz.bus.image;

import java.util.List;

import org.miaixz.bus.image.metric.Connection;

import lombok.*;
import lombok.Builder;

/**
 * Represents configurable options for a DICOM connection or request.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Option {

    /**
     * The maximum number of operations that can be invoked asynchronously on this AE. 0 means unlimited, 1 means
     * synchronous mode.
     */
    @Builder.Default
    private int maxOpsInvoked = Connection.SYNCHRONOUS_MODE;
    /**
     * The maximum number of operations that can be performed asynchronously on this AE. 0 means unlimited, 1 means
     * synchronous mode.
     */
    @Builder.Default
    private int maxOpsPerformed = Connection.SYNCHRONOUS_MODE;
    /**
     * Maximum PDU length for receiving.
     */
    @Builder.Default
    private int maxPdulenRcv = Connection.DEF_MAX_PDU_LENGTH;
    /**
     * Maximum PDU length for sending.
     */
    @Builder.Default
    private int maxPdulenSnd = Connection.DEF_MAX_PDU_LENGTH;
    /**
     * Whether to pack PDVs.
     */
    @Builder.Default
    private boolean packPDV = true;
    /**
     * The socket connection backlog.
     */
    @Builder.Default
    private int backlog = Connection.DEF_BACKLOG;
    /**
     * Connection timeout in milliseconds.
     */
    private int connectTimeout;
    /**
     * Request timeout in milliseconds.
     */
    private int requestTimeout;
    /**
     * Accept timeout in milliseconds.
     */
    private int acceptTimeout;
    /**
     * Release timeout in milliseconds.
     */
    private int releaseTimeout;
    /**
     * Response timeout in milliseconds.
     */
    private int responseTimeout;
    /**
     * Retrieve timeout in milliseconds.
     */
    private int retrieveTimeout;
    /**
     * Idle timeout in milliseconds.
     */
    private int idleTimeout;
    /**
     * Socket close delay (SO_LINGER) in seconds.
     */
    @Builder.Default
    private int socloseDelay = Connection.DEF_SOCKETDELAY;
    /**
     * Socket send buffer size (SO_SNDBUF).
     */
    private int sosndBuffer;
    /**
     * Socket receive buffer size (SO_RCVBUF).
     */
    private int sorcvBuffer;
    /**
     * Whether to enable TCP_NODELAY.
     */
    @Builder.Default
    private boolean tcpNoDelay = true;
    /**
     * A list of enabled TLS cipher suites.
     */
    @Builder.Default
    private List<String> cipherSuites = List
            .of("SSL_RSA_WITH_NULL_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA");
    /**
     * A list of enabled TLS protocols.
     */
    @Builder.Default
    private List<String> tlsProtocols = List.of("TLSv1", "SSLv3");
    /**
     * Whether client authentication is required for TLS.
     */
    private boolean tlsNeedClientAuth;
    /**
     * URL of the keystore.
     */
    private String keystoreURL;
    /**
     * Type of the keystore (e.g., JKS, PKCS12).
     */
    private String keystoreType;
    /**
     * Password for the keystore.
     */
    private String keystorePass;
    /**
     * Password for the private key within the keystore.
     */
    private String keyPass;
    /**
     * URL of the truststore.
     */
    private String truststoreURL;
    /**
     * Type of the truststore.
     */
    private String truststoreType;
    /**
     * Password for the truststore.
     */
    private String truststorePass;

}
