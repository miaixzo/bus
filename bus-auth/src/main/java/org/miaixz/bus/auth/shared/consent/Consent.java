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

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.guard.ScopeValidator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Represents an externally persisted OAuth/OIDC authorization consent snapshot without operator-permission or wire
 * response semantics.
 *
 * @param subject   stable subject reference that granted authorization
 * @param clientId  exact registered client identifier
 * @param scopes    non-empty granted OAuth scope-token set
 * @param resources ordered authorized resource indicators
 * @param grantedAt instant at which authorization was granted
 * @param expiresAt optional exclusive expiration instant
 * @author Kimi Liu
 */
public record Consent(Subject.Reference subject, String clientId, Set<String> scopes, List<String> resources,
        Instant grantedAt, Optional<Instant> expiresAt) {

    /**
     * Validates and freezes the complete granted authorization snapshot.
     *
     * @throws IllegalArgumentException if a component or collection entry is {@code null} or blank
     * @throws ValidateException        if scopes are empty or malformed, resources repeat, or expiration is not later
     */
    public Consent {
        Assert.notNull(subject, "Consent subject must not be null");
        Assert.notBlank(clientId, "Consent client identifier must not be blank");
        Assert.notNull(scopes, "Consent scope set must not be null");
        scopes = Set.copyOf(scopes);
        if (scopes.isEmpty()) {
            throw new ValidateException("Consent granted scope set must not be empty");
        }
        new ScopeValidator().validateGranted(List.copyOf(scopes), List.copyOf(scopes));
        resources = immutableResources(resources);
        Assert.notNull(grantedAt, "Consent granted-at instant must not be null");
        Assert.notNull(expiresAt, "Consent expiration container must not be null");
        expiresAt = Optional.ofNullable(expiresAt.getOrNull());
        final Instant expiration = expiresAt.getOrNull();
        if (expiration != null && !expiration.isAfter(grantedAt)) {
            throw new ValidateException("Consent expiration must be later than its grant instant");
        }
    }

    /**
     * Copies ordered resources while rejecting blank and duplicate indicators.
     *
     * @param values resource indicators
     * @return immutable ordered resource list
     */
    static List<String> immutableResources(final List<String> values) {
        Assert.notNull(values, "Consent resource list must not be null");
        final Set<String> unique = new HashSet<>(values.size());
        for (String value : values) {
            Assert.notBlank(value, "Consent resource indicator must not be blank");
            if (!unique.add(value)) {
                throw new ValidateException("Consent resource indicators must not contain duplicates");
            }
        }
        return List.copyOf(values);
    }

    /**
     * Reports whether the consent has been granted and has not reached its exclusive expiration at an instant.
     *
     * @param instant evaluation instant supplied by the shared runtime clock
     * @return {@code true} when the consent is active
     */
    public boolean activeAt(final Instant instant) {
        Assert.notNull(instant, "Consent evaluation instant must not be null");
        final Instant expiration = expiresAt.getOrNull();
        return !instant.isBefore(grantedAt) && (expiration == null || instant.isBefore(expiration));
    }

}
