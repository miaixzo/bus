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
package org.miaixz.bus.core.center.date.culture;

import org.miaixz.bus.core.center.date.Almanac;
import org.miaixz.bus.core.center.date.Culture;

/**
 * An abstract base class for traditional cultural elements, such as festivals or holidays.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class Tradition implements Culture {

    /**
     * Description inherited from parent class or interface.
     *
     * @return The name of the traditional element.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Compares this object with another based on their string representation.
     * 
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal.
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof Almanac && toString().equals(o.toString());
    }

    /**
     * Normalizes an index to be within the valid range [0, size-1] using the modulo operator.
     *
     * @param index The index to normalize.
     * @param size  The size of the cycle or range.
     * @return The normalized 0-based index.
     */
    protected int indexOf(int index, int size) {
        int i = index % size;
        if (i < 0) {
            i += size;
        }
        return i;
    }

}
