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

/**
 * Loads a project-owned external issuer/subject trust mapping for one consumer.
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface FederationLoader extends Loader<FederationLoader.Request, FederationLoader.Record> {

    /**
     * Identifies one federation relation within an exact Source registration.
     *
     * @param registration    exact Source registration requesting the relation
     * @param consumerId      public consumer identifier
     * @param assertedIssuer  verified external assertion issuer
     * @param assertedSubject verified external assertion subject
     * @author Kimi Liu
     */
    record Request(Blueprint.SourceEntry registration, String consumerId, String assertedIssuer,
            String assertedSubject) {

        /**
         * Validates one complete federation-loading request.
         */
        public Request {
            Assert.notNull(registration, "Federation registration must not be null");
            Assert.notBlank(consumerId, "Federaion consumer identifier must not be blank");
            Assert.notBlank(assertedIssuer, "Federation asserted issuer must not be blank");
            Assert.notBlank(assertedSubject, "Federation asserted subject must not be blank");
        }

    }

    /**
     * Raw project-owned federation relation.
     *
     * @param sourceId        exact Source identifier owning the relation
     * @param consumerId      exact consumer identifier owning the relation
     * @param issuer          trusted external assertion issuer
     * @param externalSubject trusted external assertion subject
     * @param subject         project subject selected by the relation
     * @author Kimi Liu
     */
    record Record(String sourceId, String consumerId, String issuer, String externalSubject, Subject.Key subject) {

    }

}
