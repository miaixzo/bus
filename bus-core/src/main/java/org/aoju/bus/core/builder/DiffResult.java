/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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

import org.aoju.bus.core.lang.Assert;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * A {@code DiffResult} contains a collection of the differences between two
 * {@link Diffable} objects. Typically these differences are displayed using
 * {@link #toString()} method, which returns a string describing the fields that
 * differ between the objects.
 * </p>
 * <p>
 * Use a {@link DiffBuilder} to build a {@code DiffResult} comparing two objects.
 * </p>
 *
 * @author Kimi Liu
 * @version 3.6.2
 * @since JDK 1.8
 */
public class DiffResult implements Iterable<Diff<?>> {

    /**
     * <p>
     * The {@code String} returned when the objects have no differences:
     * {@value}
     * </p>
     */
    public static final String OBJECTS_SAME_STRING = "";

    private static final String DIFFERS_STRING = "differs from";

    private final List<Diff<?>> diffs;
    private final Object lhs;
    private final Object rhs;
    private final ToStringStyle style;

    /**
     * <p>
     * Creates a {@link DiffResult} containing the differences between two
     * objects.
     * </p>
     *
     * @param lhs   the left hand object
     * @param rhs   the right hand object
     * @param diffs the list of differences, may be empty
     * @param style the style to use for the {@link #toString()} method. May be
     *              {@code null}, in which case
     *              {@link ToStringStyle#DEFAULT_STYLE} is used
     * @throws IllegalArgumentException if {@code lhs}, {@code rhs} or {@code diffs} is {@code null}
     */
    DiffResult(final Object lhs, final Object rhs, final List<Diff<?>> diffs,
               final ToStringStyle style) {

        Assert.isTrue(lhs != null, "Left hand object cannot be null");
        Assert.isTrue(rhs != null, "Right hand object cannot be null");
        Assert.isTrue(diffs != null, "List of differences cannot be null");

        this.diffs = diffs;
        this.lhs = lhs;
        this.rhs = rhs;

        if (style == null) {
            this.style = ToStringStyle.DEFAULT_STYLE;
        } else {
            this.style = style;
        }
    }

    /**
     * <p>
     * Returns an unmodifiable list of {@code Diff}s. The list may be empty if
     * there were no differences between the objects.
     * </p>
     *
     * @return an unmodifiable list of {@code Diff}s
     */
    public List<Diff<?>> getDiffs() {
        return Collections.unmodifiableList(diffs);
    }

    /**
     * <p>
     * Returns the number of differences between the two objects.
     * </p>
     *
     * @return the number of differences
     */
    public int getNumberOfDiffs() {
        return diffs.size();
    }

    /**
     * <p>
     * Returns the style used by the {@link #toString()} method.
     * </p>
     *
     * @return the style
     */
    public ToStringStyle getToStringStyle() {
        return style;
    }

    /**
     * <p>
     * Builds a {@code String} description of the differences contained within
     * this {@code DiffResult}. A {@link ToStringBuilder} is used for each object
     * and the style of the output is governed by the {@code ToStringStyle}
     * passed to the constructor.
     * </p>
     *
     * <p>
     * If there are no differences stored in this list, the method will return
     * {@link #OBJECTS_SAME_STRING}. Otherwise, using the example given in
     * {@link Diffable} and {@link ToStringStyle#SHORT_PREFIX_STYLE}, an output
     * might be:
     * </p>
     *
     * <pre>
     * Person[name=John Doe,age=32] differs from Person[name=Joe Bloggs,age=26]
     * </pre>
     *
     * <p>
     * This indicates that the objects differ in name and age, but not in
     * smoking status.
     * </p>
     *
     * <p>
     * To use a different {@code ToStringStyle} for an instance of this class,
     * use {@link #toString(ToStringStyle)}.
     * </p>
     *
     * @return a {@code String} description of the differences.
     */
    @Override
    public String toString() {
        return toString(style);
    }

    /**
     * <p>
     * Builds a {@code String} description of the differences contained within
     * this {@code DiffResult}, using the supplied {@code ToStringStyle}.
     * </p>
     *
     * @param style the {@code ToStringStyle} to use when outputting the objects
     * @return a {@code String} description of the differences.
     */
    public String toString(final ToStringStyle style) {
        if (diffs.isEmpty()) {
            return OBJECTS_SAME_STRING;
        }

        final ToStringBuilder lhsBuilder = new ToStringBuilder(lhs, style);
        final ToStringBuilder rhsBuilder = new ToStringBuilder(rhs, style);

        for (final Diff<?> diff : diffs) {
            lhsBuilder.append(diff.getFieldName(), diff.getLeft());
            rhsBuilder.append(diff.getFieldName(), diff.getRight());
        }

        return String.format("%s %s %s", lhsBuilder.build(), DIFFERS_STRING,
                rhsBuilder.build());
    }

    /**
     * <p>
     * Returns an iterator over the {@code Diff} objects contained in this list.
     * </p>
     *
     * @return the iterator
     */
    @Override
    public Iterator<Diff<?>> iterator() {
        return diffs.iterator();
    }

}
