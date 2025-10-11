/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http.metric.proxy;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * A {@link ProxySelector} that always returns {@link Proxy#NO_PROXY}. This can be used to effectively disable proxy
 * usage.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NullProxySelector extends ProxySelector {

    /**
     * Selects a list of proxies to use for a given URI. This implementation always returns a list containing only
     * {@link Proxy#NO_PROXY}.
     *
     * @param uri The URI to select a proxy for.
     * @return A list containing only {@link Proxy#NO_PROXY}.
     * @throws IllegalArgumentException if the URI is null.
     */
    @Override
    public List<Proxy> select(URI uri) {
        if (null == uri) {
            throw new IllegalArgumentException("URI must not be null");
        }
        return Collections.singletonList(Proxy.NO_PROXY);
    }

    /**
     * Called to indicate that a connection to a proxy has failed. This implementation does nothing.
     *
     * @param uri           The URI that the proxy failed to connect to.
     * @param socketAddress The address of the proxy that failed.
     * @param ex            The I/O exception that occurred.
     */
    @Override
    public void connectFailed(URI uri, SocketAddress socketAddress, IOException ex) {
        // Do nothing.
    }

}
