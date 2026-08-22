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

import org.miaixz.bus.auth.Loader;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Project identity input port that loads or explicitly establishes one stable subject record.
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface IdentityLoader extends Loader<ExternalIdentity, IdentityLoader.Record> {

    /**
     * Project-adapted subject data awaiting framework parsing.
     *
     * @param key        stable subject key established by the project
     * @param reference  protocol-neutral subject reference
     * @param attributes detached project-owned subject attributes
     * @author Kimi Liu
     */
    record Record(Subject.Key key, Subject.Reference reference, JsonValue.ObjectValue attributes) {

    }

}
