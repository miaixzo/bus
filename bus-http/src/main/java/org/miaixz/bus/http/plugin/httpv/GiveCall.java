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
package org.miaixz.bus.http.plugin.httpv;

/**
 * Represents an asynchronous call that can be canceled and from which a result can be retrieved.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface GiveCall extends Cancelable {

    /**
     * Returns {@code true} if this call was canceled before it completed normally.
     *
     * @return {@code true} if this call was canceled.
     */
    boolean isCanceled();

    /**
     * Returns {@code true} if this call completed. Completion may be due to normal termination, an exception, or
     * cancellation -- in all of these cases, this method will return {@code true}.
     *
     * @return {@code true} if this call completed.
     */
    boolean isDone();

    /**
     * Waits if necessary for the computation to complete, and then retrieves its result. This method will block the
     * current thread until the result is available.
     *
     * @return The result of the request execution.
     */
    CoverResult getResult();

}
