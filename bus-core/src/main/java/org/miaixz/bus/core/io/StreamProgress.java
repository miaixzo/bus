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
package org.miaixz.bus.core.io;

/**
 * Stream progress monitor. Provides stream copy progress monitoring, such as start and end triggers, and progress
 * callbacks. Note that the {@code total} parameter in the progress callback represents the total size. In some
 * scenarios where the total size is unknown, this value should be -1 or {@link Long#MAX_VALUE}, indicating that this
 * parameter is invalid.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface StreamProgress {

    /**
     * Called when the stream copy starts.
     */
    void start();

    /**
     * Called during the stream copy process.
     *
     * @param total        The total size. If unknown, it will be -1 or {@link Long#MAX_VALUE}.
     * @param progressSize The size that has already been processed.
     */
    void progress(long total, long progressSize);

    /**
     * Called when the stream copy finishes.
     */
    void finish();

}
