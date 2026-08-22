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
package org.miaixz.bus.auth.worker.loader;

import org.miaixz.bus.auth.Blueprint;
import org.miaixz.bus.auth.Loader;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Loads project-maintained subject attributes.
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface AttributeLoader extends Loader<AttributeLoader.Request, AttributeLoader.Record> {

    /**
     * Identifies the subject attributes requested within one exact Source registration.
     *
     * @param registration exact Source registration requesting the data
     * @param subject      exact external subject key
     * @author Kimi Liu
     */
    record Request(Blueprint.SourceEntry registration, Subject.Key subject) {

        /**
         * Validates one complete attribute-loading request.
         */
        public Request {
            Assert.notNull(registration, "Attribute registration must not be null");
            Assert.notNull(subject, "Attribute subject must not be null");
        }

    }

    /**
     * Loaded subject attributes awaiting framework parsing.
     *
     * @param sourceId exact Source identifier that owns the returned data
     * @param subject  exact subject key resolved by the project
     * @param values   project-provided attribute object
     * @author Kimi Liu
     */
    record Record(String sourceId, Subject.Key subject, JsonValue.ObjectValue values) {

    }

}
