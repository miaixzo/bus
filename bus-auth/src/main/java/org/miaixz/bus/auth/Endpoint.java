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
package org.miaixz.bus.auth;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Options;

/**
 * Authentication role attached to a Fabric network address.
 *
 * @param protocol wire protocol implemented by the endpoint
 * @param address  normalized Fabric network address
 * @param options  immutable endpoint-specific options
 * @author Kimi Liu
 */
public record Endpoint(Protocol protocol, Address address, Options options) {

    /**
     * Validates the endpoint and replaces null options with an empty snapshot.
     *
     * @throws ValidateException if {@code protocol} or {@code address} is null
     */
    public Endpoint {
        if (protocol == null || address == null) {
            throw new ValidateException("Endpoint protocol and Fabric address must not be null");
        }
        options = options == null ? Options.empty() : options;
    }

    /**
     * Parses a textual Fabric address into an endpoint with empty options.
     *
     * @param protocol wire protocol
     * @param address  textual Fabric address
     * @return immutable endpoint
     * @throws ValidateException if an argument is invalid
     */
    public static Endpoint of(final Protocol protocol, final String address) {
        return new Endpoint(protocol, Address.parse(address), Options.empty());
    }

    /**
     * Creates an endpoint from a parsed Fabric address with empty options.
     *
     * @param protocol wire protocol
     * @param address  parsed Fabric address
     * @return immutable endpoint
     * @throws ValidateException if an argument is invalid
     */
    public static Endpoint of(final Protocol protocol, final Address address) {
        return new Endpoint(protocol, address, Options.empty());
    }

}
