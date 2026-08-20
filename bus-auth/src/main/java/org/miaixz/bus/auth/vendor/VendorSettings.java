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
import org.miaixz.bus.auth.source.SourceSettings;
import org.miaixz.bus.core.lang.Optional;

/**
 * Defines the common immutable deployment inputs required by every third-party platform Source.
 * <p>
 * Implementations are platform records selected through {@link VendorDefinition#settingsType()}. They retain only
 * public client data and external credential references; endpoint addresses remain owned by the Vendor definition.
 * </p>
 *
 * @author Kimi Liu
 */
public interface VendorSettings extends SourceSettings {

    /**
     * Returns the stable platform routing identifier.
     *
     * @return platform identifier selected by the Vendor definition
     */
    Vendor.Id vendor();

    /**
     * Returns the exact platform product or flow variant.
     *
     * @return platform variant identifier
     */
    Vendor.Variant variant();

    /**
     * Returns the public client identifier registered with the platform.
     *
     * @return platform client identifier
     */
    String clientId();

    /**
     * Returns the external reference to the credential material required by the Vendor definition.
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
     * Returns whether an optional-PKCE Vendor definition enables S256 for this Source.
     *
     * @return {@code false} unless a platform settings record explicitly overrides the selector
     */
    default boolean pkce() {
        return false;
    }

    /**
     * Returns the tenant path-segment input used only by a definition-owned target template.
     *
     * @return tenant value or empty when the definition has no tenant template
     */
    default Optional<String> templateTenant() {
        return Optional.empty();
    }

    /**
     * Returns the platform instance host input used only by a definition-owned target template.
     *
     * @return instance value or empty when the definition has no instance template
     */
    default Optional<String> templateInstance() {
        return Optional.empty();
    }

    /**
     * Returns the authorization-server path-segment input used only by a definition-owned target template.
     *
     * @return authorization server identifier or empty when the definition has no such template
     */
    default Optional<String> templateAuthorizationServerId() {
        return Optional.empty();
    }

}
