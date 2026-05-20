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

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The uploaded file class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class UploadedFile implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852282875090L;

    private Long id;
    private Long size;
    private String filename;
    private Date createdAt;
    private UploadedByUser uploadedBy;

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
     * Returns the uploaded by.
     *
     * @return the result
     */

    public UploadedByUser getUploadedBy() {
        return uploadedBy;
    }

    /**
     * Sets the uploaded by.
     *
     * @param uploadedBy the uploaded by value
     */

    public void setUploadedBy(UploadedByUser uploadedBy) {
        this.uploadedBy = uploadedBy;
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
