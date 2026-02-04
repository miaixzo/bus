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
package org.miaixz.bus.vortex.provider;

import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.vortex.Provider;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * An implementation of {@link Provider} for serializing objects into JSON strings.
 * <p>
 * This class acts as a bridge between the gateway's format abstraction and the centralized {@link JsonKit} utility. It
 * ensures that all JSON serialization within the gateway is performed consistently and asynchronously.
 * <p>
 * Generic type parameters: {@code Provider<Object, String>}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JsonProvider implements Provider<Object, String> {

    /**
     * Asynchronously serializes the given Java object into its JSON string representation.
     * <p>
     * This implementation delegates the synchronous, CPU-bound serialization to {@link JsonKit#toJsonString(Object)}
     * and wraps it in a {@link Mono}. The work is executed on the {@code Schedulers.boundedElastic()} pool to avoid
     * blocking the event loop.
     *
     * @param input The object to be serialized.
     * @return A {@code Mono} emitting the serialized JSON string.
     */
    @Override
    public Mono<String> serialize(Object input) {
        // 1. Wrap the synchronous, blocking (CPU-bound) call in fromCallable.
        return Mono.fromCallable(() -> JsonKit.toJsonString(input))
                // 2. Offload the execution from the event loop to a safer thread pool.
                .subscribeOn(Schedulers.boundedElastic());
    }

}
