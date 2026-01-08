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
package org.miaixz.bus.http.plugin.httpv;

/**
 * Represents data that can be in any format, such as XML, YAML, Protobuf, etc., structured as an array.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface CoverArray {

    /**
     * @return The number of elements in the array.
     */
    int size();

    /**
     * @return True if the array is empty.
     */
    boolean isEmpty();

    /**
     * @param index The element index.
     * @return The sub-object as a CoverWapper.
     */
    CoverWapper getMapper(int index);

    /**
     * @param index The element index.
     * @return The sub-array as a CoverArray.
     */
    CoverArray getArray(int index);

    /**
     * @param index The element index.
     * @return The boolean value.
     */
    boolean getBool(int index);

    /**
     * @param index The element index.
     * @return The int value.
     */
    int getInt(int index);

    /**
     * @param index The element index.
     * @return The long value.
     */
    long getLong(int index);

    /**
     * @param index The element index.
     * @return The float value.
     */
    float getFloat(int index);

    /**
     * @param index The element index.
     * @return The double value.
     */
    double getDouble(int index);

    /**
     * @param index The element index.
     * @return The String value.
     */
    String getString(int index);

}
