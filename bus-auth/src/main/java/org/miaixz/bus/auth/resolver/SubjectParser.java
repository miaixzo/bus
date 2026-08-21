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

import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.worker.identity.IdentityLoader;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Pure parser for project subject records.
 */
public final class SubjectParser {

    /** Creates a stateless Subject parser. */
    public SubjectParser() {
    }

    /**
     * Parses one project-loaded stable Subject.
     *
     * @param record project identity record
     * @return immutable Subject
     */
    public Subject parse(final IdentityLoader.Record record) {
        final IdentityLoader.Record loaded = Assert.notNull(record, "Loaded Subject record must not be null");
        return new Subject(loaded.key(), loaded.reference(), loaded.attributes());
    }

    /**
     * Parses a Subject after verifying its requested reference.
     *
     * @param expected expected Subject reference
     * @param record   project identity record
     * @return immutable verified Subject
     */
    public Subject parse(final Subject.Reference expected, final IdentityLoader.Record record) {
        final Subject.Reference reference = Assert.notNull(expected, "Expected Subject reference must not be null");
        final IdentityLoader.Record loaded = Assert.notNull(record, "Loaded Subject record must not be null");
        if (!reference.equals(loaded.reference())) {
            throw new ValidateException("Loaded Subject reference does not match the requested reference");
        }
        return new Subject(loaded.key(), loaded.reference(), loaded.attributes());
    }

}
