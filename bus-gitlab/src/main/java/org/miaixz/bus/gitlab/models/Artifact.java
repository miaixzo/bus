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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * The artifact class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Artifact implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852236887008L;

    private FileType fileType;
    private Long size;
    private String filename;
    private String fileFormat;

    /**
     * Returns the file type.
     *
     * @return the result
     */

    public FileType getFileType() {
        return fileType;
    }

    /**
     * Sets the file type.
     *
     * @param fileType the file type value
     */

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    /**
     * Returns the size.
     *
     * @return the result
     */

    public Long getSize() {
        return size;
    }

    /**
     * Sets the size.
     *
     * @param size the size value
     */

    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * Returns the filename.
     *
     * @return the result
     */

    public String getFilename() {
        return filename;
    }

    /**
     * Sets the filename.
     *
     * @param filename the filename value
     */

    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Returns the file format.
     *
     * @return the result
     */

    public String getFileFormat() {
        return fileFormat;
    }

    /**
     * Sets the file format.
     *
     * @param fileFormat the file format value
     */

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
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

    /**
     * The file type enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum FileType {

        /**
         * The archive file type.
         */
        ARCHIVE,
        /**
         * The metadata file type.
         */
        METADATA,
        /**
         * The trace file type.
         */
        TRACE,
        /**
         * The junit file type.
         */
        JUNIT;

        private static JacksonJsonEnumHelper<FileType> enumHelper = new JacksonJsonEnumHelper<>(FileType.class);

        /**
         * Executes the for value operation.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static FileType forValue(String value) {
            return enumHelper.forValue(value);
        }

        /**
         * Executes the to value operation.
         *
         * @return the result
         */

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }

    }

}
