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

import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.XmlKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.vortex.Provider;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * An implementation of {@link Provider} for serializing objects into XML strings.
 * <p>
 * This provider uses a two-step process: it first converts the Java object into a generic {@code Map} using
 * {@link JsonKit}, and then serializes that map into an XML string. This means the resulting XML structure will mirror
 * the object's JSON representation.
 * <p>
 * Generic type parameters: {@code Provider<Object, String>}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XmlProvider implements Provider<Object, String> {

    /**
     * Asynchronously serializes the given Java object into its XML string representation.
     * <p>
     * This method first constructs a standard XML header. It then converts the input object into a {@code Map} and
     * subsequently serializes this map into an XML string.
     * <p>
     * The entire synchronous, CPU-bound operation is wrapped in a {@link Mono} and executed on the
     * {@code Schedulers.boundedElastic()} pool to avoid blocking the event loop.
     *
     * @param input The object to be serialized.
     * @return A {@code Mono} emitting the serialized XML string.
     */
    @Override
    public Mono<String> serialize(Object input) {
        // 1. Wrap the synchronous, blocking (CPU-bound) logic in fromCallable.
        return Mono.fromCallable(() -> {
            Map<String, Object> map = JsonKit.toMap(input);
            String buffer = XmlKit.mapToXmlString(map, "response");
            return buffer.replaceFirst(" standalone=\"[^\"]*\"", Normal.EMPTY);
        })
                // 2. Offload the execution from the event loop to a safer thread pool.
                .subscribeOn(Schedulers.boundedElastic());
    }

}
