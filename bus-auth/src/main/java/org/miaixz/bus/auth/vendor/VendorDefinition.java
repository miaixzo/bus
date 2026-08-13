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
package org.miaixz.bus.auth.vendor;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Descriptor;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.Provider;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Options;

/**
 * Immutable endpoint metadata and typed provider factory contract for one authentication vendor.
 *
 * @author Kimi Liu
 */
public interface VendorDefinition {

    /**
     * Returns the immutable endpoint map for supported vendor operations.
     *
     * @return non-null immutable endpoint map indexed by vendor operation
     */
    Map<VendorEndpoint, Endpoint> endpoints();

    /**
     * Returns the vendor's primary wire protocol.
     *
     * @return non-null Bus protocol
     */
    Protocol protocol();

    /**
     * Returns the typed factory for provider instances using explicit runtime dependencies.
     *
     * @return non-null vendor provider factory
     */
    Provider.Factory<VendorConfiguration, ? extends VendorProvider> factory();

    /**
     * Derives the root provider descriptor exclusively from this definition's identity, protocol, and endpoints.
     *
     * @return immutable root descriptor
     * @throws ValidateException if an endpoint role, endpoint, or protocol is absent, or an endpoint uses a different
     *                           protocol
     */
    default Descriptor descriptor() {
        final Protocol protocol = protocol();
        if (protocol == null) {
            throw new ValidateException("Vendor protocol must not be null");
        }
        final Map<VendorEndpoint, Endpoint> source = endpoints();
        if (source == null) {
            throw new ValidateException("Vendor endpoints must not be null");
        }
        final Map<Capability, Endpoint> resolved = new LinkedHashMap<>();
        for (final Map.Entry<VendorEndpoint, Endpoint> entry : source.entrySet()) {
            final VendorEndpoint role = entry.getKey();
            final Endpoint endpoint = entry.getValue();
            if (role == null || endpoint == null) {
                throw new ValidateException("Vendor endpoint role and endpoint must not be null");
            }
            if (endpoint.protocol() != protocol) {
                throw new ValidateException("Vendor endpoint protocol must match the vendor protocol");
            }
            resolved.put(role.capability(), endpoint);
        }
        final String name = this instanceof Enum<?> value ? value.name() : getClass().getSimpleName();
        return new Descriptor(name.toLowerCase(Locale.ROOT), name, protocol, resolved.keySet(), resolved,
                Options.empty());
    }

}
