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
package org.miaixz.bus.auth.worker;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Registration;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Loads one externally managed protocol consumer record.
 */
@FunctionalInterface
public interface ConsumerLoader {

    /**
     * Loads the record identified by the exact consumer identifier.
     *
     * @param registration exact Source registration requesting the data
     * @param consumerId   exact external consumer identifier
     * @param context      immutable non-secret invocation context
     * @param timeout      shared end-to-end operation budget
     * @return asynchronous external loading outcome
     */
    CompletionStage<Outcome<Record>> load(
            Registration.SourceEntry registration,
            String consumerId,
            Context context,
            Timeout.Budget timeout);

    /**
     * Project-adapted consumer data that has not yet been parsed by bus-auth.
     *
     * @param sourceId      exact Source identifier that owns the returned data
     * @param id            exact external consumer identifier
     * @param credential    optional project credential reference
     * @param redirectUris  registered exact redirect URI values
     * @param grantTypes    registered OAuth grant types
     * @param responseTypes registered OAuth response types
     * @param scopes        registered scope-token set
     * @param metadata      protocol-specific non-secret registration metadata
     */
    record Record(String sourceId, String id, Optional<Credential.Reference> credential, List<String> redirectUris,
            Set<String> grantTypes, Set<String> responseTypes, Set<String> scopes, JsonValue.ObjectValue metadata) {

    }

}
