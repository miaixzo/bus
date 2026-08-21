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

import java.util.List;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Options;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Defines project-supplied immutable deployment inputs for one third-party platform Source.
 * <p>
 * An implementation identifies its concrete Java type through {@link #type()}, selects one platform manifest and one
 * variant through {@link #vendor()} and {@link #variant()}, and carries only deployment-specific values. Immutable
 * platform metadata, capabilities, protocol deviations, default scopes, and endpoint targets belong to
 * {@link VariantManifest}; adapter construction belongs to {@link VendorDriver}. Options do not decode persistence,
 * resolve credentials, execute requests, or allow arbitrary replacement of manifest-owned endpoints.
 * </p>
 *
 * @param <O> exact immutable platform options implementation
 * @author Kimi Liu
 */
public interface VendorOptions<O extends VendorOptions<O>> extends Options<O> {

    /**
     * Retains built-in record options as their immutable runtime value.
     * <p>
     * A project-defined non-record implementation must override this method and return a detached immutable value.
     * </p>
     *
     * @return immutable typed Vendor options
     */
    @Override
    default O snapshot() {
        if (!getClass().isRecord()) {
            throw new ValidateException("Mutable Vendor options must provide an immutable snapshot");
        }
        return type().cast(this);
    }

    /**
     * Returns the stable platform routing identifier.
     *
     * @return platform identifier used to select one {@link VariantManifest}
     */
    Vendor.Id vendor();

    /**
     * Returns the exact platform product or flow variant.
     *
     * @return variant identifier used to select one {@link VariantManifest.Variant}
     */
    Vendor.Variant variant();

    /**
     * Returns the public client identifier registered with the platform.
     *
     * @return platform client identifier
     */
    String clientId();

    /**
     * Returns the external reference to credential material required by the selected variant.
     *
     * @return non-secret credential reference
     */
    Credential.Reference credential();

    /**
     * Returns the exact registered redirect URI lexical value for a browser variant.
     *
     * @return redirect URI or empty for a direct-only variant
     */
    Optional<String> redirectUri();

    /**
     * Returns requested platform scopes in deterministic caller order.
     *
     * @return immutable scope list
     */
    List<String> scopes();

    /**
     * Returns whether this deployment selects S256 when the manifest declares PKCE optional.
     * <p>
     * The selected {@link VariantManifest.Variant} remains the only authority for whether PKCE is disabled, optional,
     * or required. This value cannot enable a disabled variant or disable a required variant.
     * </p>
     *
     * @return {@code false} unless an optional-PKCE options value explicitly selects it
     */
    default boolean pkce() {
        return false;
    }

    /**
     * Returns the tenant path-segment value consumed only by a manifest-owned target template.
     *
     * @return tenant value or empty when the manifest has no tenant template
     */
    default Optional<String> templateTenant() {
        return Optional.empty();
    }

    /**
     * Returns the platform instance host value consumed only by a manifest-owned target template.
     *
     * @return instance value or empty when the manifest has no instance template
     */
    default Optional<String> templateInstance() {
        return Optional.empty();
    }

    /**
     * Returns the authorization-server path-segment value consumed only by a manifest-owned target template.
     *
     * @return authorization server identifier or empty when the manifest has no such template
     */
    default Optional<String> templateAuthorizationServerId() {
        return Optional.empty();
    }

}
