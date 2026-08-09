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
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumCodec;

/**
 * The import status class.
 *
 * @author Kimi Liu
 */
public class ImportStatus implements Serializable {

    /**
     * Constructs a new {@code ImportStatus} instance.
     */
    public ImportStatus() {
        // No initialization required.
    }

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852257150210L;

    /**
     * Enum representing the status of the import.
     *
     * @author Kimi Liu
     */
    public enum Status {

        /**
         * The none status.
         */
        NONE,
        /**
         * The scheduled status.
         */
        SCHEDULED,
        /**
         * The failed status.
         */
        FAILED,
        /**
         * The started status.
         */
        STARTED,
        /**
         * The finished status.
         */
        FINISHED;

        /**
         * The enum codec value.
         */
        private static JacksonJsonEnumCodec<Status> enumCodec = new JacksonJsonEnumCodec<>(Status.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static Status forValue(String value) {
            return enumCodec.forValue(value);
        }

        /**
         * Returns the value.
         *
         * @return the result
         */

        @JsonValue
        public String toValue() {
            return (enumCodec.toString(this));
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (enumCodec.toString(this));
        }

    }

    /**
     * The id value.
     */
    private Long id;
    /**
     * The description value.
     */
    private String description;
    /**
     * The name value.
     */
    private String name;
    /**
     * The name with namespace value.
     */
    private String nameWithNamespace;
    /**
     * The path value.
     */
    private String path;
    /**
     * The path with namespace value.
     */
    private String pathWithNamespace;
    /**
     * The created at value.
     */
    private Date createdAt;
    /**
     * The import status value.
     */
    private Status importStatus;
    /**
     * The import error value.
     */
    private String importError;

    /**
     * Returns the id.
     *
     * @return the result
     */

    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the id value
     */

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the description.
     *
     * @return the result
     */

    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description value
     */

    public void setDescription(String description) {
        this.description = description;
    }

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
     * Returns the name with namespace.
     *
     * @return the result
     */

    public String getNameWithNamespace() {
        return nameWithNamespace;
    }

    /**
     * Sets the name with namespace.
     *
     * @param nameWithNamespace the name with namespace value
     */

    public void setNameWithNamespace(String nameWithNamespace) {
        this.nameWithNamespace = nameWithNamespace;
    }

    /**
     * Returns the path.
     *
     * @return the result
     */

    public String getPath() {
        return path;
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
     * Returns the path with namespace.
     *
     * @return the result
     */

    public String getPathWithNamespace() {
        return pathWithNamespace;
    }

    /**
     * Sets the path with namespace.
     *
     * @param pathWithNamespace the path with namespace value
     */

    public void setPathWithNamespace(String pathWithNamespace) {
        this.pathWithNamespace = pathWithNamespace;
    }

    /**
     * Returns the created at.
     *
     * @return the result
     */

    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the created at.
     *
     * @param createdAt the created at value
     */

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the import status.
     *
     * @return the result
     */

    public Status getImportStatus() {
        return importStatus;
    }

    /**
     * Sets the import status.
     *
     * @param importStatus the import status value
     */

    public void setImportStatus(Status importStatus) {
        this.importStatus = importStatus;
    }

    /**
     * Returns the import error.
     *
     * @return the result
     */

    public String getImportError() {
        return importError;
    }

    /**
     * Sets the import error.
     *
     * @param importError the import error value
     */

    public void setImportError(String importError) {
        this.importError = importError;
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
