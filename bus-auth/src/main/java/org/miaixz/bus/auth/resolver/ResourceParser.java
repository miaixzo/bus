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
package org.miaixz.bus.auth.resolver;

import org.miaixz.bus.auth.Registration;
import org.miaixz.bus.auth.worker.ResourceLoader;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Pure parser for project-loaded protected-resource records.
 */
public final class ResourceParser {

    /** Creates a stateless protected-resource parser. */
    public ResourceParser() {
    }

    /**
     * Validates Source ownership and the exact resource lookup coordinates.
     *
     * @param registration exact Source registration that requested the resource
     * @param request      exact resource lookup request
     * @param record       project-loaded resource record
     * @return validated immutable protected-resource metadata
     */
    public ProtectedResource parse(
            final Registration.SourceEntry registration,
            final ResourceLoader.Request request,
            final ResourceLoader.Record record) {
        final String sourceId = Assert.notNull(registration, "Resource Source registration must not be null").resource()
                .getId();
        final ResourceLoader.Request expected = Assert.notNull(request, "Resource request must not be null");
        final ResourceLoader.Record loaded = Assert.notNull(record, "Loaded resource record must not be null");
        if (!sourceId.equals(loaded.sourceId()) || !expected.equals(loaded.request())) {
            throw new ValidateException("Loaded resource does not belong to the requested Source and request");
        }
        return new ProtectedResource(loaded.id(), loaded.audience(), loaded.scopes(), loaded.attributes());
    }

}
