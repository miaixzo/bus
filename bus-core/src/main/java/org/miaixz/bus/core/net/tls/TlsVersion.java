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

import org.miaixz.bus.core.net.Protocol;

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
    TLSv1_3(Protocol.TLSv1_3.name),
    /**
     * 2008年版本
     */
    TLSv1_2(Protocol.TLSv1_2.name),
    /**
     * 2006年版本
     */
    TLSv1_1(Protocol.TLSv1_1.name),
    /**
     * 1999年版本
     */
    TLSv1(Protocol.TLSv1.name),
    /**
     * 1996年版本
     */
    SSLv3(Protocol.SSLv3.name);

    public final String javaName;

    TlsVersion(String javaName) {
        this.javaName = javaName;
    }

    public static TlsVersion forJavaName(String javaName) {
        if (Protocol.TLSv1_3.name.equals(javaName)) {
            return TLSv1_3;
        }
        if (Protocol.TLSv1_2.name.equals(javaName)) {
            return TLSv1_2;
        }
        if (Protocol.TLSv1_1.name.equals(javaName)) {
            return TLSv1_1;
        }
        if (Protocol.TLSv1.name.equals(javaName)) {
            return TLSv1;
        }
        if (Protocol.SSLv3.name.equals(javaName)) {
            return SSLv3;
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
