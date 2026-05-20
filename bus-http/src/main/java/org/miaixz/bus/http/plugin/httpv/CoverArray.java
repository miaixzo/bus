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
package org.miaixz.bus.http.plugin.httpv;

/**
 * Represents data that can be in any format, such as XML, YAML, Protobuf, etc., structured as an array.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface CoverArray {

    /**
     * Returns the number of elements in the array.
     *
     * @return The number of elements in the array.
     */
    int size();

    /**
     * Tests whether the array is empty.
     *
     * @return True if the array is empty.
     */
    boolean isEmpty();

    /**
     * Returns the indexed element as an object wrapper.
     *
     * @param index The element index.
     * @return The sub-object as a CoverWapper.
     */
    CoverWapper getMapper(int index);

    /**
     * Returns the indexed element as an array wrapper.
     *
     * @param index The element index.
     * @return The sub-array as a CoverArray.
     */
    CoverArray getArray(int index);

    /**
     * Returns the indexed element as a boolean.
     *
     * @param index The element index.
     * @return The boolean value.
     */
    boolean getBool(int index);

    /**
     * Returns the indexed element as an int.
     *
     * @param index The element index.
     * @return The int value.
     */
    int getInt(int index);

    /**
     * Returns the indexed element as a long.
     *
     * @param index The element index.
     * @return The long value.
     */
    long getLong(int index);

    /**
     * Returns the indexed element as a float.
     *
     * @param index The element index.
     * @return The float value.
     */
    float getFloat(int index);

    /**
     * Returns the indexed element as a double.
     *
     * @param index The element index.
     * @return The double value.
     */
    double getDouble(int index);

    /**
     * Returns the indexed element as a string.
     *
     * @param index The element index.
     * @return The String value.
     */
    String getString(int index);

}
