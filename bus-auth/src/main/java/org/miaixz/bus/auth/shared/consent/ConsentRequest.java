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
package org.miaixz.bus.auth.shared.consent;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.guard.ScopeValidator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Carries the minimum validated, non-secret authorization context displayed by an external consent implementation.
 *
 * @param subject     stable subject reference considering the grant
 * @param clientId    validated registered client identifier
 * @param clientName  non-sensitive display name
 * @param redirectUri validated redirect URI lexical value
 * @param scopes      non-empty requested OAuth scope-token set
 * @param resources   ordered requested resource indicators
 * @author Kimi Liu
 */
public record ConsentRequest(Subject.Reference subject, String clientId, String clientName, String redirectUri,
        Set<String> scopes, List<String> resources) {

    /**
     * Validates and freezes the already protocol-validated display context.
     *
     * @throws IllegalArgumentException if a component or text value is {@code null} or blank
     * @throws ValidateException        if requested scopes are empty/malformed or resources repeat
     */
    public ConsentRequest {
        Assert.notNull(subject, "Consent request subject must not be null");
        Assert.notBlank(clientId, "Consent request client identifier must not be blank");
        Assert.notBlank(clientName, "Consent request client display name must not be blank");
        Assert.notBlank(redirectUri, "Consent request redirect URI must not be blank");
        Assert.notNull(scopes, "Consent request scope set must not be null");
        scopes = Set.copyOf(scopes);
        if (scopes.isEmpty()) {
            throw new ValidateException("Consent request scope set must not be empty");
        }
        new ScopeValidator().validateRequested(List.copyOf(scopes), scopes);
        resources = Consent.immutableResources(resources);
    }

}
