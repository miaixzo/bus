/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.thread;

import java.util.concurrent.ExecutorService;

/**
 * A specialized {@link DelegatedExecutorService} that ensures the underlying {@link ExecutorService} is properly shut
 * down when this object is garbage collected. This helps in preventing resource leaks by providing a finalization
 * mechanism for the executor service.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FinalizableDelegatedExecutorService extends DelegatedExecutorService {

    /**
     * Constructs a new {@code FinalizableDelegatedExecutorService} that wraps the given {@link ExecutorService}.
     *
     * @param executor The {@link ExecutorService} to be wrapped.
     */
    FinalizableDelegatedExecutorService(final ExecutorService executor) {
        super(executor);
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be
     * accepted. This method delegates to the underlying {@link ExecutorService#shutdown()}.
     */
    @Override
    public void shutdown() {
        super.shutdown();
    }

}
