/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.builder;

/**
 * <p>{@code Diffable} classes can be compared with other objects
 * for differences. The {@link DiffResult} object retrieved can be queried
 * for a list of differences or printed using the {@link DiffResult#toString()}.</p>
 *
 * <p>The calculation of the differences is <i>consistent with equals</i> if
 * and only if {@code d1.equals(d2)} implies {@code d1.diff(d2) == ""}.
 * It is strongly recommended that implementations are consistent with equals
 * to avoid confusion. Note that {@code null} is not an instance of any class
 * and {@code d1.diff(null)} should throw a {@code NullPointerException}.</p>
 *
 * <p>
 * {@code Diffable} classes lend themselves well to unit testing, in which a
 * easily readable description of the differences between an anticipated result and
 * an actual result can be retrieved. For example:
 * </p>
 * <pre>
 * Assert.assertEquals(expected.diff(result), expected, result);
 * </pre>
 *
 * @param <T> the type of objects that this object may be differentiated against
 * @author Kimi Liu
 * @version 3.2.6
 * @since JDK 1.8
 */
public interface Diffable<T> {

    /**
     * <p>Retrieves a list of the differences between
     * this object and the supplied object.</p>
     *
     * @param obj the object to diff against, can be {@code null}
     * @return a list of differences
     * @throws NullPointerException if the specified object is {@code null}
     */
    DiffResult diff(T obj);

}
