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
package org.miaixz.bus.core.io;

/**
 * Observes an IO transfer lifecycle. Implementations receive start, progress, and finish callbacks while data is copied
 * between streams, readers, writers, or channels. When the total length is unknown, implementations should treat
 * {@code -1} or {@link Long#MAX_VALUE} as an unavailable total length.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface TransferObserver {

    /**
     * Called when the transfer starts.
     */
    void start();

    /**
     * Called during the transfer process.
     *
     * @param total       The total size. If unknown, it will be -1 or {@link Long#MAX_VALUE}.
     * @param transferred The size that has already been transferred.
     */
    void progress(long total, long transferred);

    /**
     * Called when the transfer finishes.
     */
    void finish();

}
