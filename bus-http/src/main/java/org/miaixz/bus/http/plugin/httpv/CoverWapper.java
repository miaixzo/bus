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
