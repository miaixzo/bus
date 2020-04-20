/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.http.secure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 在协商安全插槽时可以提供的TLS版本。
 * 查看{@link javax.net.ssl.SSLSocket # setEnabledProtocols}
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public enum TlsVersion {

    /**
     * 2016年版本
     */
    TLS_1_3("TLSv1.3"),
    /**
     * 2008年版本
     */
    TLS_1_2("TLSv1.2"),
    /**
     * 2006年版本
     */
    TLS_1_1("TLSv1.1"),
    /**
     * 1999年版本
     */
    TLS_1_0("TLSv1"),
    /**
     * 1996年版本
     */
    SSL_3_0("SSLv3");

    public final String javaName;

    TlsVersion(String javaName) {
        this.javaName = javaName;
    }

    public static TlsVersion forJavaName(String javaName) {
        switch (javaName) {
            case "TLSv1.3":
                return TLS_1_3;
            case "TLSv1.2":
                return TLS_1_2;
            case "TLSv1.1":
                return TLS_1_1;
            case "TLSv1":
                return TLS_1_0;
            case "SSLv3":
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
