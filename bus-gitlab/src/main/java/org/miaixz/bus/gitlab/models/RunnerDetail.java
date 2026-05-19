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
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * The runner detail class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RunnerDetail extends Runner {

    @Serial
    private static final long serialVersionUID = 2852280720196L;

    private String architecture;
    private String platform;
    private Date contactedAt;
    private List<Project> projects;
    private String token;
    private String revision;
    private List<String> tagList;
    private String version;
    private RunnerAccessLevel accessLevel;

    /**
     * Enumeration representing the access level for a GitLab CI/CD runner.
     * <p>
     * This enum defines the protection level for runners, determining which types of projects and branches the runner
     * can execute jobs for. Access levels help control security and resource usage by restricting runner availability
     * based on reference protection.
     * </p>
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum RunnerAccessLevel {

        /**
         * The not protected defines.
         */
        NOT_PROTECTED,
        /**
         * The ref protected defines.
         */
        REF_PROTECTED;

        private static JacksonJsonEnumHelper<RunnerAccessLevel> enumHelper = new JacksonJsonEnumHelper<>(
                RunnerAccessLevel.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static RunnerAccessLevel forValue(String value) {
            return enumHelper.forValue(value);
        }

        /**
         * Returns the value.
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

    /**
     * Returns the architecture.
     *
     * @return the result
     */

    public String getArchitecture() {
        return architecture;
    }

    /**
     * Sets the architecture.
     *
     * @param architecture the architecture value
     */

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    /**
     * Returns the platform.
     *
     * @return the result
     */

    public String getPlatform() {
        return this.platform;
    }

    /**
     * Sets the platform.
     *
     * @param platform the platform value
     */

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * Returns the contacted at.
     *
     * @return the result
     */

    public Date getContactedAt() {
        return contactedAt;
    }

    /**
     * Sets the contacted at.
     *
     * @param contactedAt the contacted at value
     */

    public void setContactedAt(Date contactedAt) {
        this.contactedAt = contactedAt;
    }

    /**
     * Returns the projects.
     *
     * @return the result
     */

    public List<Project> getProjects() {
        return this.projects;
    }

    /**
     * Sets the projects.
     *
     * @param projects the projects value
     */

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    /**
     * Returns the token.
     *
     * @return the result
     */

    public String getToken() {
        return this.token;
    }

    /**
     * Sets the token.
     *
     * @param token the token value
     */

    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Returns the revision.
     *
     * @return the result
     */

    public String getRevision() {
        return this.revision;
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
     * Returns the tag list.
     *
     * @return the result
     */

    public List<String> getTagList() {
        return this.tagList;
    }

    /**
     * Sets the tag list.
     *
     * @param tagList the tag list value
     */

    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
    }

    /**
     * Returns the version.
     *
     * @return the result
     */

    public String getVersion() {
        return this.version;
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
     * Returns the access level.
     *
     * @return the result
     */

    public RunnerAccessLevel getAccessLevel() {
        return this.accessLevel;
    }

    /**
     * Sets the access level.
     *
     * @param accessLevel the access level value
     */

    public void setAccessLevel(RunnerAccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    /**
     * Sets the architecture and returns this instance.
     *
     * @param architecture the architecture value
     * @return the result
     */

    public RunnerDetail withArchitecture(String architecture) {
        this.architecture = architecture;
        return this;
    }

    /**
     * Sets the platform and returns this instance.
     *
     * @param platform the platform value
     * @return the result
     */

    public RunnerDetail withPlatform(String platform) {
        this.platform = platform;
        return this;
    }

    /**
     * Sets the contacted at and returns this instance.
     *
     * @param contactedAt the contacted at value
     * @return the result
     */

    public RunnerDetail withContactedAt(Date contactedAt) {
        this.contactedAt = contactedAt;
        return this;
    }

    /**
     * Sets the projects and returns this instance.
     *
     * @param projects the projects value
     * @return the result
     */

    public RunnerDetail withProjects(List<Project> projects) {
        this.projects = projects;
        return this;
    }

    /**
     * Sets the token and returns this instance.
     *
     * @param token the token value
     * @return the result
     */

    public RunnerDetail withToken(String token) {
        this.token = token;
        return this;
    }

    /**
     * Sets the revision and returns this instance.
     *
     * @param revision the revision value
     * @return the result
     */

    public RunnerDetail withRevision(String revision) {
        this.revision = revision;
        return this;
    }

    /**
     * Sets the tag list and returns this instance.
     *
     * @param tagList the tag list value
     * @return the result
     */

    public RunnerDetail withTagList(List<String> tagList) {
        this.tagList = tagList;
        return this;
    }

    /**
     * Sets the version and returns this instance.
     *
     * @param version the version value
     * @return the result
     */

    public RunnerDetail withVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * Sets the access level and returns this instance.
     *
     * @param accessLevel the access level value
     * @return the result
     */

    public RunnerDetail withAccessLevel(RunnerAccessLevel accessLevel) {
        this.accessLevel = accessLevel;
        return this;
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
