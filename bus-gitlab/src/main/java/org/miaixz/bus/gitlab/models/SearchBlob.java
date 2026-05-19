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
 * The search blob class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SearchBlob implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852280850317L;

    private String basename;
    private String data;
    private String filename;
    private String id;
    private String ref;
    private Integer startline;
    private Long projectId;

    /**
     * Returns the basename.
     *
     * @return the result
     */

    public String getBasename() {
        return basename;
    }

    /**
     * Sets the basename.
     *
     * @param basename the basename value
     */

    public void setBasename(String basename) {
        this.basename = basename;
    }

    /**
     * Returns the data.
     *
     * @return the result
     */

    public String getData() {
        return data;
    }

    /**
     * Sets the data.
     *
     * @param data the data value
     */

    public void setData(String data) {
        this.data = data;
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
     * Returns the id.
     *
     * @return the result
     */

    public String getId() {
        return id;
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
     * Returns the ref.
     *
     * @return the result
     */

    public String getRef() {
        return ref;
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
     * Returns the startline.
     *
     * @return the result
     */

    public Integer getStartline() {
        return startline;
    }

    /**
     * Sets the startline.
     *
     * @param startline the startline value
     */

    public void setStartline(Integer startline) {
        this.startline = startline;
    }

    /**
     * Returns the project id.
     *
     * @return the result
     */

    public Long getProjectId() {
        return projectId;
    }

    /**
     * Sets the project id.
     *
     * @param projectId the project id value
     */

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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
