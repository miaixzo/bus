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
 * Generic Activity callable contract.
 * <p>
 * This interface abstracts a common Activity shape (single request argument + single return value) so business modules
 * can reuse consistent type constraints.
 * <p>
 * Note: This interface intentionally does not carry Temporal annotations to avoid Temporal runtime scanning generic
 * type variables (type erasure), which may cause serialization/type resolution issues. Business code should define a
 * concrete Activity interface annotated with {@code @ActivityInterface}/{@code @ActivityMethod} and re-declare the
 * method with explicit parameter/return types.
 *
 * @param <R> request type
 * @param <C> return type
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ActivityCallable<R, C> {

    /**
     * Execute business logic.
     *
     * @param request request object
     * @return execution result
     */
    C execute(R request);

}
