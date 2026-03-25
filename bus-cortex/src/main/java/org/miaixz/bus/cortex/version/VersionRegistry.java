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
package org.miaixz.bus.cortex.version;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.registry.AbstractRegistry;
import org.miaixz.bus.cortex.registry.WatchManager;

/**
 * Registry for versioned artifact definitions.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class VersionRegistry extends AbstractRegistry<VersionAssets> {

    /**
     * Creates a VersionRegistry backed by the given CacheX and WatchManager.
     *
     * @param cacheX       shared cache for persistence
     * @param watchManager watch subscription manager
     */
    public VersionRegistry(CacheX<String, Object> cacheX, WatchManager watchManager) {
        super(cacheX, watchManager, VersionAssets.class, "ver");
    }

}
