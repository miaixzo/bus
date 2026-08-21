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

import org.miaixz.bus.auth.shared.claim.ClaimSet;
import org.miaixz.bus.auth.worker.identity.ClaimLoader;
import org.miaixz.bus.core.lang.Assert;

/**
 * Pure parser for project-loaded claim records.
 */
public final class ClaimParser {

    /**
     * Creates a stateless claim parser.
     */
    public ClaimParser() {
    }

    /**
     * Validates and freezes project-loaded claim entries.
     *
     * @param record project-loaded claim record
     * @return immutable framework claim set
     */
    public ClaimSet parse(final ClaimLoader.Record record) {
        final ClaimLoader.Record loaded = Assert.notNull(record, "Loaded claim record must not be null");
        return new ClaimSet(loaded.entries());
    }

}
