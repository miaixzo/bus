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
package org.miaixz.bus.core.net.tls;

import org.miaixz.bus.core.lang.Protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 在协商安全插槽时可以提供的TLS版本
 * 查看{@link javax.net.ssl.SSLSocket # setEnabledProtocols}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum TlsVersion {

    /**
     * 2016年版本
     */
    TLS_1_3(Protocol.TLS_V_13),
    /**
     * 2008年版本
     */
    TLS_1_2(Protocol.TLS_V_12),
    /**
     * 2006年版本
     */
    TLS_1_1(Protocol.TLS_V_11),
    /**
     * 1999年版本
     */
    TLS_1_0(Protocol.TLS_V_10),
    /**
     * 1996年版本
     */
    SSL_3_0(Protocol.SSL_V_30);

    public final String javaName;

    TlsVersion(String javaName) {
        this.javaName = javaName;
    }

    public static TlsVersion forJavaName(String javaName) {
        switch (javaName) {
            case Protocol.TLS_V_13:
                return TLS_1_3;
            case Protocol.TLS_V_12:
                return TLS_1_2;
            case Protocol.TLS_V_11:
                return TLS_1_1;
            case Protocol.TLS_V_10:
                return TLS_1_0;
            case Protocol.SSL_V_30:
                return SSL_3_0;
        }
        throw new IllegalArgumentException("Unexpected TLS version: " + javaName);
    }

    public static List<TlsVersion> forJavaNames(String... tlsVersions) {
        List<TlsVersion> result = new ArrayList<>(tlsVersions.length);
        for (String tlsVersion : tlsVersions) {
            result.add(forJavaName(tlsVersion));
        }
        return Collections.unmodifiableList(result);
    }

    public String javaName() {
        return javaName;
    }

}
