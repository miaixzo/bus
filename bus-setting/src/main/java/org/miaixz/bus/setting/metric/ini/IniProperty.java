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
package org.miaixz.bus.setting.metric.ini;

import java.util.Map;

/**
 * Ini file's parameters, like {@code property1=value1 }
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface IniProperty extends Map.Entry<String, String>, IniElement {

    /**
     * section getter.
     *
     * @return from section
     */
    IniSection getSection();

    /**
     * section setter.
     *
     * @param section from section
     */
    void setSection(IniSection section);

    /**
     * get key value
     *
     * @return String field: key
     */
    String key();

    /**
     * change key value.
     *
     * @param newKey new key.
     */
    void changeKey(String newKey);

    /**
     * set a new Key.
     *
     * @param newKey new Key
     * @return old value.
     */
    String setKey(String newKey);

    /**
     * get key
     *
     * @return key
     * @see #key()
     */
    @Override
    default String getKey() {
        return key();
    }

    /**
     * get value
     *
     * @return value
     * @see #value()
     */
    @Override
    default String getValue() {
        return value();
    }

}
