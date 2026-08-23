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
package org.miaixz.bus.auth.shared.claim;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Defines one explicit, script-free mapping from a verified source value to an exact target claim name and JSON type.
 *
 * @param source       source category
 * @param sourceName   exact source attribute or evidence claim name
 * @param targetName   exact target claim name
 * @param issuer       optional exact evidence issuer filter
 * @param required     whether absence without a default is an issue
 * @param sensitive    whether the resulting claim requires sensitive handling
 * @param defaultValue optional exact JsonValue used only when the source is absent
 * @author Kimi Liu
 */
public record ClaimMapping(Source source, String sourceName, String targetName, Optional<String> issuer,
        boolean required, boolean sensitive, Optional<JsonValue> defaultValue) {

    /**
     * Validates explicit mapping fields without evaluating expressions or coercing value types.
     *
     * @throws IllegalArgumentException if a component is {@code null} or required text is blank
     * @throws ValidateException        if a Subject attribute mapping declares an evidence issuer
     */
    public ClaimMapping {
        Assert.notNull(source, "Claim mapping source category must not be null");
        Assert.notBlank(sourceName, "Claim mapping source name must not be blank");
        Assert.notBlank(targetName, "Claim mapping target name must not be blank");
        Assert.notNull(issuer, "Claim mapping issuer container must not be null");
        Assert.notNull(defaultValue, "Claim mapping default-value container must not be null");
        issuer = issuer.map(value -> Assert.notBlank(value, "Claim mapping issuer must not be blank"));
        defaultValue = defaultValue.map(value -> Assert.notNull(value, "Claim mapping default value must not be null"));
        if (source == Source.SUBJECT_ATTRIBUTE && issuer.isPresent()) {
            throw new ValidateException("Subject attribute claim mapping must not declare an evidence issuer");
        }
    }

    /**
     * Enumerates the verified data categories accepted by the mapper.
     *
     * @author Kimi Liu
     */
    public enum Source {
        /**
         * Immutable Subject or parsed attribute JSON member.
         */
        SUBJECT_ATTRIBUTE,
        /**
         * Verified Evidence claim selected by exact name and optional issuer.
         */
        EVIDENCE_CLAIM

    }

}
