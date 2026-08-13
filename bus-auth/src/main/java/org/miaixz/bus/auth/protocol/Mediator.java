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

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Central type-safe entry for executing protocol handlers.
 *
 * @author Kimi Liu
 */
public final class Mediator {

    /**
     * Prevents construction of the stateless protocol mediator.
     */
    private Mediator() {
        // No initialization required.
    }

    /**
     * Executes exactly one handler and validates both asynchronous containers.
     *
     * @param handler    non-null registered protocol handler
     * @param invocation non-null authentication context
     * @param input      non-null protocol input
     * @param <I>        protocol input type
     * @param <O>        standard protocol response type
     * @return non-null stage containing a non-null internal outcome
     * @throws ValidateException if any required input, returned stage, or completed outcome is null
     */
    public static <I, O> CompletionStage<Outcome<O>> execute(
            final Handler<I, O> handler,
            final Context invocation,
            final I input) {
        final Handler<I, O> current = Assert
                .notNull(handler, () -> new ValidateException("Protocol handler must not be null"));
        final Context context = Assert
                .notNull(invocation, () -> new ValidateException("Protocol invocation must not be null"));
        final I request = Assert.notNull(input, () -> new ValidateException("Protocol input must not be null"));
        final CompletionStage<Outcome<O>> stage = Assert.notNull(
                current.handle(context, request),
                () -> new ValidateException("Protocol handler returned a null stage"));
        return stage.thenApply(
                outcome -> Assert
                        .notNull(outcome, () -> new ValidateException("Protocol handler returned a null outcome")));
    }

}
