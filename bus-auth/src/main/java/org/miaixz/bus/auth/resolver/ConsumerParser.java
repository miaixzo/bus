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

import org.miaixz.bus.auth.worker.ConsumerRecord;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/** Pure parser for externally loaded consumer records. */
public final class ConsumerParser {

    /** Parses one loaded record without performing data access. */
    public ConsumerMetadata parse(final String expectedId, final ConsumerRecord record) {
        final String expected = Assert.notBlank(expectedId, "Expected consumer identifier must not be blank");
        final ConsumerRecord loaded = Assert.notNull(record, "Loaded consumer record must not be null");
        if (!expected.equals(loaded.id())) {
            throw new ValidateException("Loaded consumer identifier does not match the requested identifier");
        }
        return new ConsumerMetadata(loaded.id(), loaded.credential(), loaded.redirectUris(), loaded.grantTypes(),
                loaded.responseTypes(), loaded.scopes(), loaded.metadata());
    }
}
