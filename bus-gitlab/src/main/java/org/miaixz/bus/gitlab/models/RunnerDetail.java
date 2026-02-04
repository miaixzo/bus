/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab.models;

import java.util.Date;
import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serial;

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
     * @since 17
     */
    public enum RunnerAccessLevel {

        /**
         * Indicates the runner is not protected.
         * <p>
         * This runner can execute jobs for all projects regardless of their visibility, including public and internal
         * projects. It is the default access level for shared runners and provides no access restrictions.
         * </p>
         */
        NOT_PROTECTED,

        /**
         * Indicates the runner is reference protected.
         * <p>
         * This runner can only execute jobs for protected references (branches and tags) in projects. Protected
         * references are typically main branches or release tags that have additional security restrictions. This
         * access level is useful for runners with access to sensitive resources or deployment credentials.
         * </p>
         */
        REF_PROTECTED;

        private static JacksonJsonEnumHelper<RunnerAccessLevel> enumHelper = new JacksonJsonEnumHelper<>(
                RunnerAccessLevel.class);

        @JsonCreator
        public static RunnerAccessLevel forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getPlatform() {
        return this.platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Date getContactedAt() {
        return contactedAt;
    }

    public void setContactedAt(Date contactedAt) {
        this.contactedAt = contactedAt;
    }

    public List<Project> getProjects() {
        return this.projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRevision() {
        return this.revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public List<String> getTagList() {
        return this.tagList;
    }

    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public RunnerAccessLevel getAccessLevel() {
        return this.accessLevel;
    }

    public void setAccessLevel(RunnerAccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public RunnerDetail withArchitecture(String architecture) {
        this.architecture = architecture;
        return this;
    }

    public RunnerDetail withPlatform(String platform) {
        this.platform = platform;
        return this;
    }

    public RunnerDetail withContactedAt(Date contactedAt) {
        this.contactedAt = contactedAt;
        return this;
    }

    public RunnerDetail withProjects(List<Project> projects) {
        this.projects = projects;
        return this;
    }

    public RunnerDetail withToken(String token) {
        this.token = token;
        return this;
    }

    public RunnerDetail withRevision(String revision) {
        this.revision = revision;
        return this;
    }

    public RunnerDetail withTagList(List<String> tagList) {
        this.tagList = tagList;
        return this;
    }

    public RunnerDetail withVersion(String version) {
        this.version = version;
        return this;
    }

    public RunnerDetail withAccessLevel(RunnerAccessLevel accessLevel) {
        this.accessLevel = accessLevel;
        return this;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
