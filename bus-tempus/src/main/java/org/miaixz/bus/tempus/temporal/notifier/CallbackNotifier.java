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
package org.miaixz.bus.tempus.temporal.notifier;

/**
 * Receives activity completion notifications.
 * <p>
 * Implementations can propagate execution outcomes to external systems or perform additional post-processing after
 * activity completion.
 *
 * @param <R> the activity input type
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface CallbackNotifier<R> {

    /**
     * Notifies a successful activity execution.
     *
     * @param request the activity input
     * @param result  the execution result
     */
    default void success(R request, Object result) {

    }

    /**
     * Notifies a failed activity execution.
     *
     * @param request  the activity input
     * @param errorMsg the failure message
     */
    default void failure(R request, String errorMsg) {

    }

}
