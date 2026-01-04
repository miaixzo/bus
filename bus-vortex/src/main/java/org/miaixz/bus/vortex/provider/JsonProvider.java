/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
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
