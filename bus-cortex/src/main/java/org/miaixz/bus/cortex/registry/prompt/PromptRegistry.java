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
package org.miaixz.bus.cortex.registry.prompt;

import java.util.List;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.registry.RegistryStore;
import org.miaixz.bus.cortex.registry.StoreBackedRegistry;
import org.miaixz.bus.cortex.magic.watch.WatchManager;

/**
 * Registry for prompt template definitions.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PromptRegistry extends StoreBackedRegistry<PromptAssets> {

    /**
     * Creates a PromptRegistry backed by the given CacheX and WatchManager.
     *
     * @param cacheX       shared cache for registry state
     * @param watchManager watch subscription manager
     * @param store        durable store adapter
     */
    public PromptRegistry(CacheX<String, Object> cacheX, WatchManager watchManager, RegistryStore<PromptAssets> store) {
        super(cacheX, watchManager, store, PromptAssets.class, Type.PROMPT, List.of());
    }

}
