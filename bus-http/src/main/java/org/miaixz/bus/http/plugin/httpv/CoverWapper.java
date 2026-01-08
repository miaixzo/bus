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

import java.util.Set;

/**
 * Represents data that can be in any format, such as XML, YAML, Protobuf, etc., structured as a key-value map.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface CoverWapper {

    /**
     * @return The number of key-value pairs.
     */
    int size();

    /**
     * @return True if the map is empty.
     */
    boolean isEmpty();

    /**
     * @param key The key name.
     * @return The sub-object as a CoverWapper.
     */
    CoverWapper getWappers(String key);

    /**
     * @param key The key name.
     * @return The sub-array as a CoverArray.
     */
    CoverArray getArray(String key);

    /**
     * @param key The key name.
     * @return The boolean value.
     */
    boolean getBool(String key);

    /**
     * @param key The key name.
     * @return The int value.
     */
    int getInt(String key);

    /**
     * @param key The key name.
     * @return The long value.
     */
    long getLong(String key);

    /**
     * @param key The key name.
     * @return The float value.
     */
    float getFloat(String key);

    /**
     * @param key The key name.
     * @return The double value.
     */
    double getDouble(String key);

    /**
     * @param key The key name.
     * @return The String value.
     */
    String getString(String key);

    /**
     * @param key The key name.
     * @return True if the key exists.
     */
    boolean has(String key);

    /**
     * @return The set of keys.
     */
    Set<String> keySet();

}
