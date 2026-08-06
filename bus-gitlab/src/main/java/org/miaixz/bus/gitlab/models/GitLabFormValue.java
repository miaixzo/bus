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
package org.miaixz.bus.gitlab.models;

/**
 * The Git lab form value class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class GitLabFormValue {

    private Object value;

    /**
     * The type field.
     */
    private GitLabFormValueType type;
    private boolean required;

    /**
     * Constructs a new {@code GitLabFormValue} instance.
     *
     * @param value    the value value
     * @param type     the type value
     * @param required the required value
     */

    public GitLabFormValue(Object value, GitLabFormValueType type, boolean required) {
        super();
        this.value = value;
        this.type = type;
        this.required = required;
    }

    /**
     * Returns the value.
     *
     * @return the result
     */

    public Object getValue() {
        return value;
    }

    /**
     * Returns the type.
     *
     * @return the result
     */

    public GitLabFormValueType getType() {
        return type;
    }

    /**
     * Returns whether the required is enabled.
     *
     * @return the result
     */

    public boolean isRequired() {
        return required;
    }

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return "GitLabFormValue [value=" + value + ", type=" + type + ", required=" + required + "]";
    }

}
