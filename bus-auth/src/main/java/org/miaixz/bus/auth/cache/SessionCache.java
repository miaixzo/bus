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
package org.miaixz.bus.auth.cache;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.FabricX.Clock;
import org.miaixz.bus.auth.Session;
import org.miaixz.bus.cache.CacheX;

/**
 * Stores the framework's single root Session model by an isolated session-key digest.
 * <p>
 * The wrapper delegates atomic create and replace for lifecycle transitions and delete for explicit session ending to
 * bus-cache. It does not define a second Session type, retain an ID Token, or invent subject/client indexes absent from
 * the root Session; integrating projects may maintain such associations in their own persistence model.
 * </p>
 *
 * @author Kimi Liu
 */
public class SessionCache extends AuthCache<Session> {

    /**
     * Isolates authentication sessions from every other bus-cache consumer.
     */
    private static final String PURPOSE = "session";

    /**
     * Creates a Session cache view backed entirely by bus-cache.
     *
     * @param cache      shared bus-cache backend
     * @param deployment deployment-unique cache namespace
     * @param clock      shared runtime clock used to derive entry lifetimes
     */
    public SessionCache(final CacheX<String, Object> cache, final String deployment, final Clock clock) {
        super(cache, deployment, PURPOSE, Session.class, clock);
    }

    /**
     * Creates a Source-generation-scoped Session cache view for compiled runtime use.
     *
     * @param cache      shared bus-cache backend
     * @param deployment deployment-unique cache namespace
     * @param sourceId   exact Source registration identifier
     * @param generation non-negative Source configuration generation
     * @param clock      shared runtime clock used to derive entry lifetimes
     */
    public SessionCache(final CacheX<String, Object> cache, final String deployment, final String sourceId,
            final long generation, final Clock clock) {
        super(cache, deployment, PURPOSE, Session.class, sourceId, generation, clock);
    }

    /**
     * Establishes a new Session when absent.
     *
     * @param key   Session digest
     * @param value Session and expiry
     * @return creation stage
     */
    public CompletionStage<Boolean> establish(final String key, final ExpiringValue<Session> value) {
        return super.doIssue(key, value);
    }

    /**
     * Finds current Session state.
     *
     * @param key Session digest
     * @return stored Session stage
     */
    public CompletionStage<ExpiringValue<Session>> find(final String key) {
        return super.doFind(key);
    }

    /**
     * Atomically replaces exact Session state.
     *
     * @param key      Session digest
     * @param expected current state
     * @param update   replacement state
     * @return replacement stage
     */
    public CompletionStage<Boolean> refresh(
            final String key,
            final ExpiringValue<Session> expected,
            final ExpiringValue<Session> update) {
        return super.doUpdate(key, expected, update);
    }

    /**
     * Invalidates Session state.
     *
     * @param key Session digest
     * @return removal stage
     */
    public CompletionStage<Boolean> invalidate(final String key) {
        return super.doRevoke(key);
    }

}
