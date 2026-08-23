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

import java.util.List;

import org.miaixz.bus.auth.Identity;
import org.miaixz.bus.auth.Loader;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.shared.claim.ClaimSet;
import org.miaixz.bus.core.lang.Assert;

/**
 * Loads project-disclosed claim records for one verified identity and stable subject.
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface ClaimLoader extends Loader<ClaimLoader.Request, ClaimLoader.Record> {

    /**
     * Binds the stable subject to the verified external identity used for claim loading.
     *
     * @param subject  stable framework subject
     * @param identity verified completed external identity
     * @author Kimi Liu
     */
    record Request(Subject subject, Identity identity) {

        /**
         * Validates one complete claim-loading request.
         */
        public Request {
            Assert.notNull(subject, "Claim loading Subject must not be null");
            Assert.notNull(identity, "Claim loading external identity must not be null");
        }

    }

    /**
     * Project-loaded claim entries awaiting framework parsing.
     *
     * @param entries ordered claim entries disclosed by the project
     * @author Kimi Liu
     */
    record Record(List<ClaimSet.Entry> entries) {

    }

}
