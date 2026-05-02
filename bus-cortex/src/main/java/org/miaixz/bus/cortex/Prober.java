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
package org.miaixz.bus.cortex;

/**
 * Contract for server-side active health probing.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Prober {

    /**
     * Probes the given instance and returns a health result.
     *
     * @param instance instance to probe
     * @return health check result
     */
    Status check(Instance instance);

    /**
     * Returns whether this prober can handle the given instance.
     *
     * @param instance runtime instance
     * @return {@code true} when the prober can evaluate the instance
     */
    default boolean supports(Instance instance) {
        return instance != null;
    }

    /**
     * Returns the logical capability name of the prober.
     *
     * @return prober name
     */
    default String name() {
        return getClass().getSimpleName();
    }

}
