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

import org.miaixz.bus.auth.Registry;

/**
 * Receives safe notifications after a Registry reload is committed or rejected.
 * <p>
 * Implementations may publish metrics or audit signals but must not access credential material or invoke Registry
 * business capabilities. The reload service isolates listener failures, so notification failure cannot create a partial
 * commit or roll back an already committed immutable view. Callbacks are ordered observations outside the commit
 * transaction; a bounded queue may discard the oldest observation under sustained overload, and runtime close discards
 * observations that have not started.
 * </p>
 *
 * @author Kimi Liu
 */
public interface RegistryListener {

    /**
     * Reports that bounded asynchronous delivery discarded older observations.
     * <p>
     * Implementations must treat notifications as best-effort projections and rebuild from the current Registry when
     * this callback occurs. It is not an audit transaction and cannot affect an already committed revision.
     * </p>
     *
     * @param discarded      number of observations discarded since the previous overflow callback
     * @param latestCommitted latest committed revision known by the notifier
     */
    default void overflow(final long discarded, final Registry.Revision latestCommitted) {
        // Optional observation-gap hook.
    }

    /**
     * Receives the revision after its complete immutable view has been committed.
     *
     * @param revision committed Registry revision
     */
    void committed(Registry.Revision revision);

    /**
     * Receives a safe report when a desired snapshot is rejected before commit.
     *
     * @param report immutable rejection report without secret options
     */
    void rejected(Registry.Report report);

}
