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
package org.miaixz.bus.auth.protocol.scim;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;

/**
 * Identifies a SCIM resource collection or one resource through its registered ResourceType.
 * <p>
 * The target does not accept an endpoint string. HTTP codecs derive the path from {@link Scim} and the validated
 * {@link ResourceType}, preventing arbitrary route injection and keeping routing separate from JSON bodies.
 * </p>
 *
 * @param resourceType registered resource-type discovery object
 * @param resourceId   optional service-provider-issued resource identifier
 * @author Kimi Liu
 */
public record ResourceTarget(ResourceType resourceType, Optional<String> resourceId) {

    /**
     * Validates and freezes one resource target.
     *
     * @throws IllegalArgumentException if a component or optional container is {@code null}
     */
    public ResourceTarget {
        resourceType = Assert.notNull(resourceType, "SCIM target ResourceType must not be null");
        Assert.notNull(resourceId, "SCIM target resource id container must not be null");
        final String id = resourceId.getOrNull();
        if (id != null) {
            Assert.notBlank(id, "SCIM target resource id must not be blank");
        }
        resourceId = Optional.ofNullable(id);
    }

    /**
     * Returns a diagnostic summary without the resource identifier.
     *
     * @return redacted target representation
     */
    @Override
    public String toString() {
        return "ResourceTarget[resourceType=" + resourceType.name() + ",resourceId=[REDACTED]]";
    }

}
