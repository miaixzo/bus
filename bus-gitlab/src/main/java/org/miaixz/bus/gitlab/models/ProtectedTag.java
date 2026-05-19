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
import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The protected tag class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ProtectedTag implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852275572097L;

    /**
     * The create access level class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class CreateAccessLevel implements Serializable {

        @Serial
        private static final long serialVersionUID = 2852275621375L;

        private AccessLevel access_level;
        private String accessLevelDescription;

        /**
         * Returns the access level.
         *
         * @return the result
         */

        public AccessLevel getAccess_level() {
            return access_level;
        }

        /**
         * Sets the access level.
         *
         * @param access_level the access level value
         */

        public void setAccess_level(AccessLevel access_level) {
            this.access_level = access_level;
        }

        /**
         * Returns the access level description.
         *
         * @return the result
         */

        public String getAccessLevelDescription() {
            return accessLevelDescription;
        }

        /**
         * Sets the access level description.
         *
         * @param accessLevelDescription the access level description value
         */

        public void setAccessLevelDescription(String accessLevelDescription) {
            this.accessLevelDescription = accessLevelDescription;
        }

    }

    private String name;
    private List<CreateAccessLevel> createAccessLevels;

    /**
     * Returns the name.
     *
     * @return the result
     */

    public String getName() {
        return name;
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
     * Returns the create access levels.
     *
     * @return the result
     */

    public List<CreateAccessLevel> getCreateAccessLevels() {
        return createAccessLevels;
    }

    /**
     * Sets the create access levels.
     *
     * @param createAccessLevels the create access levels value
     */

    public void setCreateAccessLevels(List<CreateAccessLevel> createAccessLevels) {
        this.createAccessLevels = createAccessLevels;
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
