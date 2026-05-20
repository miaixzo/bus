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

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.miaixz.bus.gitlab.support.ISO8601;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The changelog payload class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ChangelogPayload implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852238869802L;

    private String version;
    private String from;
    private String to;
    private Date date;
    private String branch;
    private String trailer;
    private String file;
    private String message;

    /**
     * Constructs a new {@code ChangelogPayload} instance.
     *
     * @param version the version value
     */

    public ChangelogPayload(String version) {
        this.version = version;
    }

    /**
     * Returns the form data.
     *
     * @return the result
     */

    @JsonIgnore
    public GitLabForm getFormData() {
        return new GitLabForm().withParam("version", version, true).withParam("from", from).withParam("to", to)
                .withParam("date", ISO8601.dateOnly(date)).withParam("branch", branch).withParam("trailer", trailer)
                .withParam("file", file).withParam("message", message);
    }

    /**
     * Returns the version.
     *
     * @return the result
     */

    public String getVersion() {
        return version;
    }

    /**
     * Sets the version.
     *
     * @param version the version value
     */

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the from.
     *
     * @return the result
     */

    public String getFrom() {
        return from;
    }

    /**
     * Sets the from.
     *
     * @param from the from value
     */

    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Returns the to.
     *
     * @return the result
     */

    public String getTo() {
        return to;
    }

    /**
     * Sets the to.
     *
     * @param to the to value
     */

    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Returns the date.
     *
     * @return the result
     */

    public Date getDate() {
        return date;
    }

    /**
     * Sets the date.
     *
     * @param date the date value
     */

    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Returns the branch.
     *
     * @return the result
     */

    public String getBranch() {
        return branch;
    }

    /**
     * Sets the branch.
     *
     * @param branch the branch value
     */

    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * Returns the trailer.
     *
     * @return the result
     */

    public String getTrailer() {
        return trailer;
    }

    /**
     * Sets the trailer.
     *
     * @param trailer the trailer value
     */

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    /**
     * Returns the file.
     *
     * @return the result
     */

    public String getFile() {
        return file;
    }

    /**
     * Sets the file.
     *
     * @param file the file value
     */

    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Returns the message.
     *
     * @return the result
     */

    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     *
     * @param message the message value
     */

    public void setMessage(String message) {
        this.message = message;
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
