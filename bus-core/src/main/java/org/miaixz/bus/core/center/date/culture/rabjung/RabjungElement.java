/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.date.culture.rabjung;

import org.miaixz.bus.core.center.date.culture.Element;

/**
 * Represents the Five Elements in the Tibetan calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RabjungElement extends Element {

    /**
     * Constructs a {@code RabjungElement} with the given index.
     *
     * @param index The index of the element.
     */
    public RabjungElement(int index) {
        super(index);
    }

    /**
     * Constructs a {@code RabjungElement} with the given name.
     *
     * @param name The name of the element. "铁" (Iron) will be replaced with "金" (Metal).
     */
    public RabjungElement(String name) {
        super(name.replace("铁", "金"));
    }

    /**
     * Creates a {@code RabjungElement} instance from the given index.
     *
     * @param index The index of the element.
     * @return A new {@link RabjungElement} instance.
     */
    public static RabjungElement fromIndex(int index) {
        return new RabjungElement(index);
    }

    /**
     * Creates a {@code RabjungElement} instance from the given name.
     *
     * @param name The name of the element.
     * @return A new {@link RabjungElement} instance.
     */
    public static RabjungElement fromName(String name) {
        return new RabjungElement(name);
    }

    /**
     * Gets the next Rabjung element after a specified number of steps.
     *
     * @param n The number of steps to advance.
     * @return The {@link RabjungElement} after {@code n} steps.
     */
    public RabjungElement next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the element that this element generates (我生者).
     *
     * @return The generated {@link RabjungElement}.
     */
    public RabjungElement getReinforce() {
        return next(1);
    }

    /**
     * Gets the element that this element overcomes (我克者).
     *
     * @return The overcome {@link RabjungElement}.
     */
    public RabjungElement getRestrain() {
        return next(2);
    }

    /**
     * Gets the element that generates this element (生我者).
     *
     * @return The generating {@link RabjungElement}.
     */
    public RabjungElement getReinforced() {
        return next(-1);
    }

    /**
     * Gets the element that overcomes this element (克我者).
     *
     * @return The overcoming {@link RabjungElement}.
     */
    public RabjungElement getRestrained() {
        return next(-2);
    }

    @Override
    public String getName() {
        return super.getName().replace("金", "铁");
    }

}
