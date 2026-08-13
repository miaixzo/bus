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
package org.miaixz.bus.auth.protocol;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;

/**
 * Common execution contract implemented by authentication and identity protocol engines.
 *
 * @param <I> protocol input type
 * @param <O> protocol output type
 * @author Kimi Liu
 */
public interface Handler<I, O> extends Provider {

    /**
     * {@inheritDoc}
     */
    @Override
    Descriptor descriptor();

    /**
     * Tests whether the immutable descriptor declares one capability.
     *
     * @param capability capability to test
     * @return whether this handler supports the capability
     */
    default boolean supports(final Capability capability) {
        return descriptor().supports(capability);
    }

    /**
     * Executes one protocol operation. Successful values and domain failures remain internal until the concrete
     * protocol entry maps them to its standard wire response.
     *
     * @param invocation non-null authentication context
     * @param input      non-null protocol input
     * @return non-null stage containing a non-null outcome whose success value is a protocol-standard response
     */
    CompletionStage<Outcome<O>> handle(Context invocation, I input);

    /**
     * Creates a fully assembled protocol handler from explicit typed configuration.
     *
     * @param <C> complete handler configuration type
     * @param <H> handler type
     * @author Kimi Liu
     */
    @FunctionalInterface
    interface Factory<C, H extends Handler<?, ?>> extends Provider.Factory<C, H> {
    }

}
