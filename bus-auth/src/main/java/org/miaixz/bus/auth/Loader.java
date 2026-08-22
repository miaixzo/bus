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
package org.miaixz.bus.auth;

import java.util.concurrent.CompletionStage;

/**
 * Defines one asynchronous project-owned data input port.
 * <p>
 * A request contains only the validated coordinates required by one loading operation. Implementations obtain raw
 * project data but do not parse authentication-domain records, apply protocol policy, mutate Registry state, or reset
 * the caller's timeout.
 * </p>
 *
 * @param <Q> validated loading request type
 * @param <R> loaded raw record type
 * @author Kimi Liu
 */
@FunctionalInterface
public interface Loader<Q, R> {

    /**
     * Loads project-owned data within the caller's existing authentication operation.
     *
     * @param request validated loading coordinates
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation timeout
     * @return asynchronous project loading outcome
     */
    CompletionStage<Outcome<R>> load(Q request, Context context, Timeout timeout);

}
