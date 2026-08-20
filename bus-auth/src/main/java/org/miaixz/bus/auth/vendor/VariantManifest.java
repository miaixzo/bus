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
 * Declares the framework-owned immutable authentication manifest for exactly one third-party platform and all variants
 * supported for that platform.
 * <p>
 * A manifest contains only platform identity, presentation metadata, and immutable {@link Variant} facts. Project
 * deployment input remains in {@link VendorOptions}; manifest-to-factory assembly remains in {@link VendorDriver};
 * execution remains in {@link VendorAdapter}. A manifest performs no persistence decoding, credential resolution,
 * global registration, Source compilation, network operation, or authentication execution.
 * </p>
 *
 * @param <O> exact immutable options type accepted by this platform manifest
 * @author Kimi Liu
 */
public interface VariantManifest<O extends VendorOptions<?>> {

    /**
     * Returns the stable identifier of the platform described by this manifest.
     *
     * @return platform identifier
     */
    Vendor.Id vendor();

    /**
     * Returns immutable presentation metadata for management and discovery views.
     *
     * @return management presentation metadata
     */
    Vendor.Metadata metadata();

    /**
     * Returns all supported variants in deterministic declaration order.
     *
     * @return immutable non-empty variants
     */
    List<Variant> variants();

    /**
     * Returns the unique platform variant under the requested identifier.
     *
     * @param variant requested variant identifier
     * @return exact immutable variant
     * @throws ValidateException if the variant is unsupported
     */
    Variant variant(Vendor.Variant variant);

    /**
     * Carries the framework-owned immutable authentication facts for one exact platform variant.
     *
     * @param platform           stable identifier of the owning platform manifest
     * @param variant            stable platform variant identifier
     * @param protocol           actual industry-standard or proprietary wire protocol
     * @param defaultScopes      ordered framework defaults for authorization requests
     * @param targets            official fixed or constrained-template platform targets
     * @param capabilityManifest fully implemented capability manifest
     * @param deviations         documented platform deviations from the selected protocol
     * @author Kimi Liu
     */
    record Variant(Vendor.Id platform, Vendor.Variant variant, Protocol protocol, List<String> defaultScopes,
            VendorTargets targets, Capability.Manifest capabilityManifest, List<VendorDeviation> deviations) {

        /**
         * Validates and freezes one platform variant.
         *
         * @throws IllegalArgumentException if a component or collection item is {@code null}
         */
        public Variant {
            platform = Assert.notNull(platform, "Variant manifest platform must not be null");
            variant = Assert.notNull(variant, "Variant manifest identifier must not be null");
            protocol = Assert.notNull(protocol, "Variant manifest protocol must not be null");
            Assert.notNull(defaultScopes, "Variant manifest default scopes must not be null");
            final List<String> scopes = new ArrayList<>(defaultScopes.size());
            for (String scope : defaultScopes) {
                scopes.add(Assert.notBlank(scope, "Variant manifest default scope must not be blank"));
            }
            defaultScopes = List.copyOf(scopes);
            targets = Assert.notNull(targets, "Variant manifest targets must not be null");
            capabilityManifest = Assert.notNull(capabilityManifest, "Variant capability manifest must not be null");
            Assert.notNull(deviations, "Variant manifest deviations must not be null");
            final List<VendorDeviation> copy = new ArrayList<>(deviations.size());
            for (VendorDeviation deviation : deviations) {
                copy.add(Assert.notNull(deviation, "Variant manifest deviation must not be null"));
            }
            deviations = List.copyOf(copy);
        }

    }

}
