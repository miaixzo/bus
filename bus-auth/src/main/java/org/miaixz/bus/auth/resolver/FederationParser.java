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

import org.miaixz.bus.auth.Blueprint;
import org.miaixz.bus.auth.worker.loader.FederationLoader;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Pure parser for project-loaded federation relations.
 *
 * @author Kimi Liu
 */
public class FederationParser {

    /**
     * Creates a stateless parser.
     */
    public FederationParser() {
        // No initialization required.
    }

    /**
     * Validates exact Source, consumer, issuer, and external subject ownership.
     */
    public FederationMetadata parse(
            final Blueprint.SourceEntry registration,
            final String expectedConsumerId,
            final String expectedIssuer,
            final String expectedExternalSubject,
            final FederationLoader.Record record) {
        final FederationLoader.Record loaded = Assert.notNull(record, "Loaded federation record must not be null");
        final String sourceId = Assert.notNull(registration, "Federation Source must not be null").resource().getId();
        if (!sourceId.equals(loaded.sourceId()) || !expectedConsumerId.equals(loaded.consumerId())
                || !expectedIssuer.equals(loaded.issuer())
                || !expectedExternalSubject.equals(loaded.externalSubject())) {
            throw new ValidateException("Loaded federation relation does not match the requested relation");
        }
        return new FederationMetadata(loaded.consumerId(), loaded.issuer(), loaded.externalSubject(), loaded.subject());
    }

}
