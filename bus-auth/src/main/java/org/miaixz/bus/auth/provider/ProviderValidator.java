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
package org.miaixz.bus.auth.provider;

import org.miaixz.bus.auth.Provider;
import org.miaixz.bus.core.lang.Assert;

/**
 * Validates one protocol-neutral Provider persistence entity.
 * <p>
 * Validation covers only direct entity invariants. Library ownership and child Source relationships are checked against
 * the complete registration snapshot by the Registry validator.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ProviderValidator {

    public ProviderValidator() {
        // Stateless validator.
    }

    /**
     * Validates Provider identity, presentation, ordering, and required direct Library reference.
     *
     * @param value complete Provider entity
     * @throws IllegalArgumentException if the Provider or a required field is {@code null} or blank
     */
    public void validate(final Provider value) {
        Assert.notNull(value, "Provider must not be null");
        Assert.notBlank(value.getLibrary_id(), "Provider library id must not be blank");
        Assert.notBlank(value.getCode(), "Provider code must not be blank");
        Assert.notBlank(value.getName(), "Provider name must not be blank");
        if (value.getSort() != null) {
            Assert.isTrue(value.getSort() >= 0, "Provider sort must not be negative");
        }
    }

}
