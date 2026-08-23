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
package org.miaixz.bus.auth.registry;

import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;

/**
 * Carries one field-level Blueprint invariant violation without assigning processing-stage ownership.
 *
 * @param field       violated Blueprint field name
 * @param error       stable validation error classification
 * @param description safe human-readable violation description
 * @author Kimi Liu
 */
record FieldViolation(String field, Errors error, String description) {

    /**
     * Validates one complete field-level invariant violation.
     */
    FieldViolation {
        Assert.notBlank(field, "Field violation name must not be blank");
        Assert.notNull(error, "Field violation error must not be null");
        Assert.notBlank(description, "Field violation description must not be blank");
    }

}
