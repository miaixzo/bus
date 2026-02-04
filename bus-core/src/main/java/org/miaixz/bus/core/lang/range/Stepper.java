/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.range;

/**
 * Stepper interface. This interface is used to define how an object should be incremented or decremented in steps. The
 * stepper can define the following logic:
 *
 * <pre>
 * 1. Stepping rules: how the object should be stepped.
 * 2. Step size: by implementing this interface and defining an object property in the implementation class,
 *    the step size can be flexibly defined.
 * 3. Limit on the number of ranges: by implementing this interface and defining an object property in the implementation class,
 *    a limit can be flexibly defined to restrict the number of ranges.
 * </pre>
 *
 * @param <T> the type of object to be stepped
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface Stepper<T> {

    /**
     * Increments the object by one step. If the return value after stepping is {@code null}, it indicates that the
     * stepping has ended. The user must define the boundary logic based on the {@code end} parameter; when the boundary
     * is reached, return {@code null} to signify completion. Otherwise, the boundary object in {@code Range} will be
     * ineffective, potentially leading to an infinite loop.
     *
     * @param current the base object from the previous step
     * @param end     the ending object of the range
     * @param index   the current index (which element in the step sequence), starting from 0
     * @return the object after incrementing by one step, or {@code null} if the stepping has ended
     */
    T step(T current, T end, int index);

}
