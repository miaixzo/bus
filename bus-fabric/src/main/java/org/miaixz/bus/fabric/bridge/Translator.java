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
package org.miaixz.bus.fabric.bridge;

/**
 * Translator contract that converts external ingestions into concrete fabric targets.
 *
 * @param <T> translated target type
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Translator<T> {

    /**
     * Translates an external ingestion into a concrete target.
     *
     * @param ingestion immutable external metadata and payload to adapt
     * @return protocol-specific target produced by this translator
     */
    T translate(Ingestion ingestion);

    /**
     * Returns whether this translator supports an ingestion.
     *
     * @param ingestion immutable external input to inspect without translation
     * @return {@code true} when this translator recognizes the ingestion
     */
    boolean supports(Ingestion ingestion);

}
