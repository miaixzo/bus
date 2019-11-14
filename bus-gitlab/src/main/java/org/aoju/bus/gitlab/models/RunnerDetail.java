package org.aoju.bus.gitlab.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.aoju.bus.gitlab.utils.JacksonJson;
import org.aoju.bus.gitlab.utils.JacksonJsonEnumHelper;

import java.util.Date;
import java.util.List;

public class RunnerDetail extends Runner {

    private String architecture;
    private String platform;
    private Date contactedAt;
    private List<org.aoju.bus.gitlab.models.Project> projects;
    private String token;
    private String revision;
    private List<String> tagList;
    private String version;
    private RunnerAccessLevel accessLevel;

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

    public List<org.aoju.bus.gitlab.models.Project> getProjects() {
        return this.projects;
    }

    public void setProjects(List<org.aoju.bus.gitlab.models.Project> projects) {
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

    /**
     * Enum to use for RunnerDetail accessLevel property.
     */
    public enum RunnerAccessLevel {

        NOT_PROTECTED, REF_PROTECTED;
        private static JacksonJsonEnumHelper<RunnerAccessLevel> enumHelper =
                new JacksonJsonEnumHelper<>(RunnerAccessLevel.class);

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
}
