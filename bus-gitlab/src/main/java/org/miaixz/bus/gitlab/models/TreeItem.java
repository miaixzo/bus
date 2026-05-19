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

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The tree item class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TreeItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852282298357L;

    /**
     * The type enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Type {

        /**
         * The tree type.
         */
        TREE,
        /**
         * The blob type.
         */
        BLOB,
        /**
         * The commit type.
         */
        COMMIT;

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (name().toLowerCase());
        }

    }

    private String id;
    private String mode;
    private String name;
    private String path;

    /**
     * The type field.
     */
    private Type type;

    /**
     * Returns the id.
     *
     * @return the result
     */

    public String getId() {
        return this.id;
    }

    /**
     * Sets the id.
     *
     * @param id the id value
     */

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the mode.
     *
     * @return the result
     */

    public String getMode() {
        return this.mode;
    }

    /**
     * Sets the mode.
     *
     * @param mode the mode value
     */

    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * Returns the name.
     *
     * @return the result
     */

    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     *
     * @param name the name value
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the path.
     *
     * @return the result
     */

    public String getPath() {
        return this.path;
    }

    /**
     * Sets the path.
     *
     * @param path the path value
     */

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns the type.
     *
     * @return the result
     */

    public Type getType() {
        return this.type;
    }

    /**
     * Sets the type.
     *
     * @param type the type value
     */

    public void setType(Type type) {
        this.type = type;
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
