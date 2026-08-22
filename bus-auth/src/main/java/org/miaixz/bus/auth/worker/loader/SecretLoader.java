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
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Loader;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.core.lang.Assert;

/**
 * Loads a fresh externally owned secret lease.
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface SecretLoader extends Loader<SecretLoader.Request, SecretLoader.Record> {

    /**
     * Identifies one credential requested within an exact Source registration.
     *
     * @param registration exact Source registration requesting the secret
     * @param reference    exact project credential reference
     * @author Kimi Liu
     */
    record Request(Blueprint.SourceEntry registration, Credential.Reference reference) {

        /**
         * Validates one complete secret-loading request.
         */
        public Request {
            Assert.notNull(registration, "Secret registration must not be null");
            Assert.notNull(reference, "Secret credential reference must not be null");
        }

    }

    /**
     * Loaded secret lease paired with its exact external reference.
     *
     * @param sourceId  exact Source identifier that owns the returned data
     * @param reference exact credential reference resolved by the project
     * @param lease     fresh closeable secret lease
     * @author Kimi Liu
     */
    record Record(String sourceId, Credential.Reference reference, SecretLease lease) {

    }

}
