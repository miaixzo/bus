/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
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
