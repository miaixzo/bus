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
package org.miaixz.bus.errors.spi;

import java.util.List;

import org.miaixz.bus.errors.ErrorContext;
import org.miaixz.bus.errors.ErrorDecision;
import org.miaixz.bus.errors.ErrorType;

/**
 * Receives safe attempt or terminal events from the operation boundary. Observer failures are handled by the
 * boundary through a separate diagnostic channel.
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface ErrorObserver {

    /**
     * Default observer for applications without a registered telemetry sink.
     */
    ErrorObserver NOOP = event -> {
    };

    /**
     * Receives an event without request content or raw exceptions.
     *
     * @param event safe error event
     */
    void observe(Event event);

    /**
     * Receives an event together with bounded diagnostics. Existing metrics and tracing observers keep receiving the
     * original event through their single-argument callback.
     *
     * @param event   structured failure event
     * @param details approved diagnostic fields without the exception message or original throwable
     */
    default void observe(Event event, Details details) {
        observe(event);
    }

    /**
     * Requests a stack snapshot only when the observer can use it.
     *
     * @param event structured failure event
     * @return maximum number of desired frames, or zero for event-only observation
     */
    default int stackFrameLimit(Event event) {
        return 0;
    }

    /**
     * Carries bounded diagnostics without request inputs or a live exception reference.
     *
     * @param action        selected disposition, or null when unavailable
     * @param basis         classification source, or null when unavailable
     * @param requestId     verified server request identifier, or null
     * @param outcome       response write signal, or null
     * @param exceptionType exception class, or null
     * @param frames        bounded list of class, method and line locations
     * @author Kimi Liu
     */
    record Details(ErrorDecision.Action action, ErrorDecision.Basis basis, String requestId, String outcome,
            String exceptionType,
            List<String> frames) {

        /**
         * Detaches the frame list from the caller.
         *
         * @param action        selected disposition
         * @param basis         classification source
         * @param requestId     verified request identifier
         * @param outcome       response write signal
         * @param exceptionType exception class
         * @param frames        approved frame locations
         */
        public Details {
            frames = List.copyOf(frames);
        }

    }

    /**
     * Describes an attempt or final failure using bounded-cardinality data.
     *
     * @param context  execution boundary
     * @param type     approved semantic type
     * @param status   verified status or zero
     * @param terminal whether this is the final operation outcome
     * @author Kimi Liu
     */
    record Event(ErrorContext context, ErrorType type, int status, boolean terminal) {
    }

}
