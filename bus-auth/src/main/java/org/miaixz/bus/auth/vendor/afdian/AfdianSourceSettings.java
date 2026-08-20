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
package org.miaixz.bus.auth.vendor.afdian;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorSettings;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Carries the externally managed Afdian client registration values.
 *
 * @param vendor      exact Afdian routing identifier
 * @param variant     exact Afdian flow variant
 * @param clientId    public Afdian client identifier
 * @param credential  external client-secret reference
 * @param redirectUri exact registered callback URI
 * @param scopes      ordered requested Afdian scopes
 * @author Kimi Liu
 */
public record AfdianSourceSettings(Vendor.Id vendor, Vendor.Variant variant, String clientId,
        Credential.Reference credential, Optional<String> redirectUri, List<String> scopes) implements VendorSettings {

    /**
     * Validates and freezes one Afdian Source registration.
     *
     * @throws IllegalArgumentException if a required component is null or blank
     * @throws ValidateException        if routing, credential type, or callback policy differs from the frozen
     *                                  definition
     */
    public AfdianSourceSettings {
        if (!AfdianDefinition.ID.equals(vendor) || !AfdianDefinition.DEFAULT.equals(variant)) {
            throw new ValidateException("Afdian settings routing keys must select afdian/default");
        }
        Assert.notBlank(clientId, "Afdian client id must not be blank");
        Assert.notNull(credential, "Afdian credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Afdian credential must reference a client secret");
        }
        Assert.notNull(redirectUri, "Afdian redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isPresent()) {
            Assert.notBlank(redirectUri.getOrNull(), "Afdian redirect URI must not be blank");
        }
        Assert.notNull(scopes, "Afdian scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            copy.add(Assert.notBlank(scope, "Afdian scope must not be blank"));
        }
        scopes = List.copyOf(copy);
    }

}
