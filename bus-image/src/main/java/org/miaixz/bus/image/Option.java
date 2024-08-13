/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image;

import java.util.List;

import org.miaixz.bus.image.metric.Connection;

import lombok.*;
import lombok.Builder;

/**
 * 请求可选项
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Option {

    /**
     * 此AE可以异步执行的最大操作数，无限制为0，而非异步为1
     */
    @Builder.Default
    private int maxOpsInvoked = Connection.SYNCHRONOUS_MODE;
    @Builder.Default
    private int maxOpsPerformed = Connection.SYNCHRONOUS_MODE;
    @Builder.Default
    private int maxPdulenRcv = Connection.DEF_MAX_PDU_LENGTH;
    @Builder.Default
    private int maxPdulenSnd = Connection.DEF_MAX_PDU_LENGTH;
    @Builder.Default
    private boolean packPDV = true;
    @Builder.Default
    private int backlog = Connection.DEF_BACKLOG;

    private int connectTimeout;
    private int requestTimeout;
    private int acceptTimeout;
    private int releaseTimeout;
    private int responseTimeout;
    private int retrieveTimeout;
    private int idleTimeout;
    @Builder.Default
    private int socloseDelay = Connection.DEF_SOCKETDELAY;
    private int sosndBuffer;
    private int sorcvBuffer;
    @Builder.Default
    private boolean tcpNoDelay = true;
    @Builder.Default
    private List<String> cipherSuites = List.of("SSL_RSA_WITH_NULL_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA",
            "SSL_RSA_WITH_3DES_EDE_CBC_SHA");
    @Builder.Default
    private List<String> tlsProtocols = List.of("TLSv1", "SSLv3");

    private boolean tlsNeedClientAuth;
    private String keystoreURL;
    private String keystoreType;
    private String keystorePass;
    private String keyPass;
    private String truststoreURL;
    private String truststoreType;
    private String truststorePass;

}
