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
 * The package file class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PackageFile implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852268389591L;

    private Long id;
    private Long packageId;
    private Date createdAt;
    private String fileName;
    private Long size;
    private String fileMd5;
    private String fileSha1;
    private String fileSha256;

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
     * Returns the package id.
     *
     * @return the result
     */

    public Long getPackageId() {
        return packageId;
    }

    /**
     * Sets the package id.
     *
     * @param packageId the package id value
     */

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
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
     * Returns the file name.
     *
     * @return the result
     */

    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the file name.
     *
     * @param fileName the file name value
     */

    public void setFileName(String fileName) {
        this.fileName = fileName;
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
     * Returns the file md5.
     *
     * @return the result
     */

    public String getFileMd5() {
        return fileMd5;
    }

    /**
     * Sets the file md5.
     *
     * @param fileMd5 the file md5 value
     */

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    /**
     * Returns the file sha1.
     *
     * @return the result
     */

    public String getFileSha1() {
        return fileSha1;
    }

    /**
     * Sets the file sha1.
     *
     * @param fileSha1 the file sha1 value
     */

    public void setFileSha1(String fileSha1) {
        this.fileSha1 = fileSha1;
    }

    /**
     * Returns the file sha256.
     *
     * @return the result
     */

    public String getFileSha256() {
        return fileSha256;
    }

    /**
     * Sets the file sha256.
     *
     * @param fileSha256 the file sha256 value
     */

    public void setFileSha256(String fileSha256) {
        this.fileSha256 = fileSha256;
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
