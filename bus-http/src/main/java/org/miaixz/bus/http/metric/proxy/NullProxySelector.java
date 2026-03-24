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
 * @since Java 21+
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
