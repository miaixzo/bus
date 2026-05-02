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
package org.miaixz.bus.cortex.magic.identity;

import org.miaixz.bus.core.lang.Normal;

/**
 * Common Cortex identity normalization shared by registry, setting, watch, and guard code.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class CortexIdentity {

    /**
     * Creates the shared identity utility holder.
     */
    private CortexIdentity() {

    }

    /**
     * Returns the canonical namespace, defaulting blank values to the shared default namespace.
     *
     * @param namespace raw namespace value
     * @return canonical namespace value
     */
    public static String namespace(String namespace) {
        return namespace == null || namespace.isBlank() ? Normal.DEFAULT : namespace;
    }

    /**
     * Returns the canonical application identifier.
     *
     * @param app_id raw application identifier
     * @return canonical application identifier or {@code null} when blank
     */
    public static String applicationId(String app_id) {
        return app_id == null || app_id.isBlank() ? null : app_id;
    }

}
