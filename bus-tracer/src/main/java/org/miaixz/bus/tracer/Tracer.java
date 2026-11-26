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
package org.miaixz.bus.tracer;

import org.miaixz.bus.errors.ErrorBoundary;
import org.miaixz.bus.errors.spi.ErrorObserver;

/**
 * Connects safe Bus failure events to an application-owned tracing sink.
 *
 * @author Kimi Liu
 */
public class Tracer {

    /**
     * Constructs a new Tracer instance.
     */
    public Tracer() {
        // No initialization required.
    }

    /**
     * Attaches a tracing sink to the standard component failure pipeline. The sink runs on the reporting thread, so it
     * can attach the event to the active trace according to the application's tracing policy.
     *
     * @param observer tracing sink for safe component failure events
     * @return subscription to close with the tracing integration
     */
    public ErrorBoundary.Subscription observeErrors(ErrorObserver observer) {
        return ErrorBoundary.subscribeStandard(observer);
    }

}
