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
package org.miaixz.bus.image.builtin.hp;

/**
 * Hanging Protocol sorting operation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum SortingOperation {

    /**
     * Constant for the along axis value.
     */
    ALONG_AXIS("ALONG_AXIS"),
    /**
     * Constant for the by acq time value.
     */
    BY_ACQ_TIME("BY_ACQ_TIME");

    /**
     * The code string value.
     */
    private final String codeString;

    /**
     * Creates a new instance.
     *
     * @param codeString the code string.
     */
    SortingOperation(String codeString) {
        this.codeString = codeString;
    }

    /**
     * Gets the code string.
     *
     * @return the code string.
     */
    public String getCodeString() {
        return codeString;
    }

}
