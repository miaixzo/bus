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

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Declares immutable platform metadata and its independently supported authentication variants.
 *
 * @param <S> exact immutable settings object used by this platform
 * @author Kimi Liu
 */
public interface VendorDefinition<S extends VendorSettings> extends org.miaixz.bus.core.Provider<Vendor.Id> {

    /**
     * Returns the stable platform identifier.
     *
     * @return platform identifier
     */
    @Override
    Vendor.Id type();

    /**
     * Returns immutable platform presentation metadata.
     *
     * @return management presentation metadata
     */
    Vendor.Metadata metadata();

    /**
     * Returns the exact settings object accepted for this platform.
     *
     * @return platform settings class
     */
    Class<S> settingsType();

    /**
     * Returns all supported variants in deterministic declaration order.
     *
     * @return immutable non-empty variants
     */
    List<Definition> variants();

    /**
     * Returns one unique platform variant definition.
     *
     * @param variant requested variant identifier
     * @return exact definition
     * @throws ValidateException if the variant is unsupported
     */
    Definition variant(Vendor.Variant variant);

    /**
     * Carries only immutable platform facts required to compile one variant.
     *
     * @param platform      stable third-party platform identifier
     * @param variant       stable platform variant identifier
     * @param protocol      actual industry-standard or proprietary Bus protocol
     * @param defaultScopes ordered default scopes for authorization requests
     * @param targets       official immutable platform targets
     * @param manifest      fully implemented capability manifest
     * @param deviations    proven platform deviations from the selected protocol
     * @author Kimi Liu
     */
    record Definition(Vendor.Id platform, Vendor.Variant variant, Protocol protocol, List<String> defaultScopes,
            VendorTargets targets, Capability.Manifest manifest, List<VendorDeviation> deviations) {

        /**
         * Validates and freezes one platform variant definition.
         *
         * @throws IllegalArgumentException if a component or collection item is {@code null}
         */
        public Definition {
            platform = Assert.notNull(platform, "Vendor definition platform must not be null");
            variant = Assert.notNull(variant, "Vendor definition variant must not be null");
            protocol = Assert.notNull(protocol, "Vendor definition protocol must not be null");
            Assert.notNull(defaultScopes, "Vendor definition default scopes must not be null");
            final List<String> scopes = new ArrayList<>(defaultScopes.size());
            for (String scope : defaultScopes) {
                scopes.add(Assert.notBlank(scope, "Vendor default scope must not be blank"));
            }
            defaultScopes = List.copyOf(scopes);
            targets = Assert.notNull(targets, "Vendor definition targets must not be null");
            manifest = Assert.notNull(manifest, "Vendor definition manifest must not be null");
            Assert.notNull(deviations, "Vendor definition deviations must not be null");
            final List<VendorDeviation> copy = new ArrayList<>(deviations.size());
            for (VendorDeviation deviation : deviations) {
                copy.add(Assert.notNull(deviation, "Vendor definition deviation must not be null"));
            }
            deviations = List.copyOf(copy);
        }

    }

}
