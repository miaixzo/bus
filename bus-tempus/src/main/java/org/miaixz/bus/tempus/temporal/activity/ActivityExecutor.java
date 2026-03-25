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
package org.miaixz.bus.tempus.temporal.activity;

/**
 * Executes an activity for a supported input type.
 * <p>
 * Implementations encapsulate the business-specific execution logic for a particular category of activity inputs.
 *
 * @param <R> the activity input type
 * @param <C> the activity context type
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface ActivityExecutor<R, C> {

    /**
     * Returns whether this executor supports the specified input.
     *
     * @param request the activity input
     * @return {@code true} if this executor supports the input; {@code false} otherwise
     */
    boolean supports(R request);

    /**
     * Executes the activity.
     *
     * @param request the activity input
     * @param context the execution context
     * @return the execution result
     * @throws Exception if activity execution fails
     */
    Object execute(R request, C context) throws Exception;

}
