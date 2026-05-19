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
package org.miaixz.bus.gitlab.hooks.system;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The repository change class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RepositoryChange {

    private String after;
    private String before;
    private String ref;

    /**
     * Returns the after.
     *
     * @return the result
     */

    public String getAfter() {
        return this.after;
    }

    /**
     * Sets the after.
     *
     * @param after the after value
     */

    public void setAfter(String after) {
        this.after = after;
    }

    /**
     * Returns the before.
     *
     * @return the result
     */

    public String getBefore() {
        return this.before;
    }

    /**
     * Sets the before.
     *
     * @param before the before value
     */

    public void setBefore(String before) {
        this.before = before;
    }

    /**
     * Returns the ref.
     *
     * @return the result
     */

    public String getRef() {
        return this.ref;
    }

    /**
     * Sets the ref.
     *
     * @param ref the ref value
     */

    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
