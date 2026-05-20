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
 * The registry repository tag class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RegistryRepositoryTag implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852277267977L;

    private String name;
    private String path;
    private String location;
    private String revision;
    private String shortRevision;
    private String digest;
    private Date createdAt;
    private Long totalSize;

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
     * Returns the location.
     *
     * @return the result
     */

    public String getLocation() {
        return location;
    }

    /**
     * Sets the location.
     *
     * @param location the location value
     */

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the revision.
     *
     * @return the result
     */

    public String getRevision() {
        return revision;
    }

    /**
     * Sets the revision.
     *
     * @param revision the revision value
     */

    public void setRevision(String revision) {
        this.revision = revision;
    }

    /**
     * Returns the short revision.
     *
     * @return the result
     */

    public String getShortRevision() {
        return shortRevision;
    }

    /**
     * Sets the short revision.
     *
     * @param shortRevision the short revision value
     */

    public void setShortRevision(String shortRevision) {
        this.shortRevision = shortRevision;
    }

    /**
     * Returns the digest.
     *
     * @return the result
     */

    public String getDigest() {
        return digest;
    }

    /**
     * Sets the digest.
     *
     * @param digest the digest value
     */

    public void setDigest(String digest) {
        this.digest = digest;
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
     * Returns the total size.
     *
     * @return the result
     */

    public Long getTotalSize() {
        return totalSize;
    }

    /**
     * Sets the total size.
     *
     * @param totalSize the total size value
     */

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
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
