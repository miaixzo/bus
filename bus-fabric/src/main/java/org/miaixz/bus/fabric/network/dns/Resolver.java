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
package org.miaixz.bus.fabric.network.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Host resolver contract with a default JDK DNS implementation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Resolver {

    /**
     * Resolves a host to addresses.
     *
     * @param host host
     * @return immutable address list
     */
    default List<InetAddress> resolve(final String host) {
        final String validHost = validateHost(host);
        try {
            return List.of(InetAddress.getAllByName(validHost));
        } catch (final UnknownHostException e) {
            throw new SocketException("Unable to resolve host " + validHost, e);
        }
    }

    /**
     * Returns whether this resolver supports a host.
     *
     * @param host host
     * @return true when supported
     */
    default boolean supports(final String host) {
        validateHost(host);
        return true;
    }

    /**
     * Validates host names.
     *
     * @param host host
     * @return normalized host
     */
    private static String validateHost(final String host) {
        if (StringKit.isBlank(host) || StringKit.containsAny(host, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Resolver host must be non-blank and single-line");
        }
        return host.trim();
    }

}
