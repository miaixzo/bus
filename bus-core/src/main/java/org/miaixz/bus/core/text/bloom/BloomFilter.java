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
package org.miaixz.bus.core.text.bloom;

import java.io.Serializable;

/**
 * Bloom filter is a binary vector data structure proposed by Howard Bloom in 1970. It offers excellent space and time
 * efficiency and is used to check if an element is a member of a set. If the check result is positive, the element may
 * or may not be in the set; however, if the check result is negative, the element is definitely not in the set.
 * Therefore, a Bloom filter has a 100% recall rate. Each check request can return two outcomes: "in the set (possibly
 * false positive)" and "not in the set (definitely not in the set)".
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface BloomFilter extends Serializable {

    /**
     * Checks if the given text is contained in the Bloom filter. If the text is definitely not in the filter, it
     * returns {@code false}. If the text might be in the filter (with a possibility of false positive), it returns
     * {@code true}.
     *
     * @param text The string to check.
     * @return {@code true} if the text might be in the filter, {@code false} if it is definitely not in the filter.
     */
    boolean contains(String text);

    /**
     * Adds a string to the Bloom filter. If the string already exists in the filter, it returns {@code false}. If the
     * string does not exist, it adds the string and returns {@code true}.
     *
     * @param text The string to add.
     * @return {@code true} if the string was added successfully, {@code false} if it already existed.
     */
    boolean add(String text);

}
