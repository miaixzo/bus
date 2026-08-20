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
package org.miaixz.bus.auth.runtime;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.core.lang.Assert;

/** Maps a successful external loading outcome through one explicitly supplied pure parser. */
public final class LoadResult {

    private LoadResult() {
    }

    /**
     * Preserves loader rejection and failure outcomes and parses only a successfully loaded record.
     *
     * @param loading external loading stage
     * @param parser  pure loaded-record parser
     * @param <S>     loaded record type
     * @param <T>     parsed domain type
     * @return asynchronous outcome containing the parsed domain value
     */
    public static <S, T> CompletionStage<Outcome<T>> parse(
            final CompletionStage<Outcome<S>> loading,
            final Function<S, T> parser) {
        Assert.notNull(loading, "Loading stage must not be null");
        Assert.notNull(parser, "Loaded-record parser must not be null");
        return loading.thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<S> success -> Outcome.succeeded(parser.apply(success.value()));
            case Outcome.Rejected<S> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<S> failed -> Outcome.failed(failed.failure());
        });
    }
}
