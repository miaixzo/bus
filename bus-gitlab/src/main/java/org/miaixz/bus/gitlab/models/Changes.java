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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The changes class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Changes implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852238919699L;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("a_mode")
    private String a_mode;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("b_mode")
    private String b_mode;

    private Boolean deletedFile;
    private String diff;
    private Boolean newFile;
    private String newPath;
    private String oldPath;
    private Boolean renamedFile;

    /**
     * Returns the amode.
     *
     * @return the result
     */

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("a_mode")
    public String getAMode() {
        return a_mode;
    }

    /**
     * Sets the amode.
     *
     * @param a_mode the a mode value
     */

    public void setAMode(String a_mode) {
        this.a_mode = a_mode;
    }

    /**
     * Returns the bmode.
     *
     * @return the result
     */

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("b_mode")
    public String getBMode() {
        return b_mode;
    }

    /**
     * Sets the bmode.
     *
     * @param b_mode the b mode value
     */

    public void setBMode(String b_mode) {
        this.b_mode = b_mode;
    }

    /**
     * Returns the deleted file.
     *
     * @return the result
     */

    public Boolean getDeletedFile() {
        return deletedFile;
    }

    /**
     * Sets the deleted file.
     *
     * @param deletedFile the deleted file value
     */

    public void setDeletedFile(Boolean deletedFile) {
        this.deletedFile = deletedFile;
    }

    /**
     * Returns the diff.
     *
     * @return the result
     */

    public String getDiff() {
        return diff;
    }

    /**
     * Sets the diff.
     *
     * @param diff the diff value
     */

    public void setDiff(String diff) {
        this.diff = diff;
    }

    /**
     * Returns the new file.
     *
     * @return the result
     */

    public Boolean getNewFile() {
        return newFile;
    }

    /**
     * Sets the new file.
     *
     * @param newFile the new file value
     */

    public void setNewFile(Boolean newFile) {
        this.newFile = newFile;
    }

    /**
     * Returns the new path.
     *
     * @return the result
     */

    public String getNewPath() {
        return newPath;
    }

    /**
     * Sets the new path.
     *
     * @param newPath the new path value
     */

    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    /**
     * Returns the old path.
     *
     * @return the result
     */

    public String getOldPath() {
        return oldPath;
    }

    /**
     * Sets the old path.
     *
     * @param oldPath the old path value
     */

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    /**
     * Returns the renamed file.
     *
     * @return the result
     */

    public Boolean getRenamedFile() {
        return renamedFile;
    }

    /**
     * Sets the renamed file.
     *
     * @param renamedFile the renamed file value
     */

    public void setRenamedFile(Boolean renamedFile) {
        this.renamedFile = renamedFile;
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
