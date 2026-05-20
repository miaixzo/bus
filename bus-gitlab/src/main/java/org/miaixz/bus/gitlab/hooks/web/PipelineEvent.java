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
package org.miaixz.bus.gitlab.hooks.web;

import java.io.Serial;
import java.util.Date;
import java.util.List;

import org.miaixz.bus.gitlab.models.Build;
import org.miaixz.bus.gitlab.models.Job;
import org.miaixz.bus.gitlab.models.Variable;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The pipeline event class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PipelineEvent extends AbstractEvent {

    @Serial
    private static final long serialVersionUID = 2852232997522L;
    /**
     * The x gitlab event value.
     */

    public static final String X_GITLAB_EVENT = "Pipeline Hook";
    /**
     * The object kind value.
     */
    public static final String OBJECT_KIND = "pipeline";

    private ObjectAttributes objectAttributes;
    private EventMergeRequest mergeRequest;
    private EventUser user;
    private EventProject project;
    private EventCommit commit;
    private List<Job> jobs;
    private List<Build> builds;

    /**
     * Returns the object kind.
     *
     * @return the result
     */

    public String getObjectKind() {
        return (OBJECT_KIND);
    }

    /**
     * Sets the object kind.
     *
     * @param objectKind the object kind value
     */

    public void setObjectKind(String objectKind) {
        if (!OBJECT_KIND.equals(objectKind))
            throw new RuntimeException("Invalid object_kind (" + objectKind + "), must be '" + OBJECT_KIND + "'");
    }

    /**
     * Returns the object attributes.
     *
     * @return the result
     */

    public ObjectAttributes getObjectAttributes() {
        return this.objectAttributes;
    }

    /**
     * Sets the object attributes.
     *
     * @param objectAttributes the object attributes value
     */

    public void setObjectAttributes(ObjectAttributes objectAttributes) {
        this.objectAttributes = objectAttributes;
    }

    /**
     * Returns the merge request.
     *
     * @return the result
     */

    public EventMergeRequest getMergeRequest() {
        return mergeRequest;
    }

    /**
     * Sets the merge request.
     *
     * @param mergeRequest the merge request value
     */

    public void setMergeRequest(EventMergeRequest mergeRequest) {
        this.mergeRequest = mergeRequest;
    }

    /**
     * Returns the user.
     *
     * @return the result
     */

    public EventUser getUser() {
        return user;
    }

    /**
     * Sets the user.
     *
     * @param user the user value
     */

    public void setUser(EventUser user) {
        this.user = user;
    }

    /**
     * Returns the project.
     *
     * @return the result
     */

    public EventProject getProject() {
        return project;
    }

    /**
     * Sets the project.
     *
     * @param project the project value
     */

    public void setProject(EventProject project) {
        this.project = project;
    }

    /**
     * Returns the commit.
     *
     * @return the result
     */

    public EventCommit getCommit() {
        return commit;
    }

    /**
     * Sets the commit.
     *
     * @param commit the commit value
     */

    public void setCommit(EventCommit commit) {
        this.commit = commit;
    }

    /**
     * Returns the jobs.
     *
     * @return the result
     */

    public List<Job> getJobs() {
        return jobs;
    }

    /**
     * Sets the jobs.
     *
     * @param jobs the jobs value
     */

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    /**
     * Returns the builds.
     *
     * @return the result
     */

    public List<Build> getBuilds() {
        return builds;
    }

    /**
     * Sets the builds.
     *
     * @param builds the builds value
     */

    public void setBuilds(List<Build> builds) {
        this.builds = builds;
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
     * The object attributes class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class ObjectAttributes {

        private Long id;
        private Long iid;
        private String name;
        private String ref;
        private Boolean tag;
        private String sha;
        private String beforeSha;
        private String source;
        private String status;
        private String detailedStatus;
        private List<String> stages;
        private Date createdAt;
        private Date finishedAt;
        private Integer duration;
        private Float queuedDuration;
        private List<Variable> variables;
        private String url;

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
         * Returns the iid.
         *
         * @return the result
         */

        public Long getIid() {
            return iid;
        }

        /**
         * Sets the iid.
         *
         * @param iid the iid value
         */

        public void setIid(Long iid) {
            this.iid = iid;
        }

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
         * Returns the tag.
         *
         * @return the result
         */

        public Boolean getTag() {
            return tag;
        }

        /**
         * Sets the tag.
         *
         * @param tag the tag value
         */

        public void setTag(Boolean tag) {
            this.tag = tag;
        }

        /**
         * Returns the sha.
         *
         * @return the result
         */

        public String getSha() {
            return sha;
        }

        /**
         * Sets the sha.
         *
         * @param sha the sha value
         */

        public void setSha(String sha) {
            this.sha = sha;
        }

        /**
         * Returns the before sha.
         *
         * @return the result
         */

        public String getBeforeSha() {
            return beforeSha;
        }

        /**
         * Sets the before sha.
         *
         * @param beforeSha the before sha value
         */

        public void setBeforeSha(String beforeSha) {
            this.beforeSha = beforeSha;
        }

        /**
         * Returns the source.
         *
         * @return the result
         */

        public String getSource() {
            return source;
        }

        /**
         * Sets the source.
         *
         * @param source the source value
         */

        public void setSource(String source) {
            this.source = source;
        }

        /**
         * Returns the status.
         *
         * @return the result
         */

        public String getStatus() {
            return status;
        }

        /**
         * Sets the status.
         *
         * @param status the status value
         */

        public void setStatus(String status) {
            this.status = status;
        }

        /**
         * Returns the detailed status.
         *
         * @return the result
         */

        public String getDetailedStatus() {
            return detailedStatus;
        }

        /**
         * Sets the detailed status.
         *
         * @param detailedStatus the detailed status value
         */

        public void setDetailedStatus(String detailedStatus) {
            this.detailedStatus = detailedStatus;
        }

        /**
         * Returns the stages.
         *
         * @return the result
         */

        public List<String> getStages() {
            return stages;
        }

        /**
         * Sets the stages.
         *
         * @param stages the stages value
         */

        public void setStages(List<String> stages) {
            this.stages = stages;
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
         * Returns the finished at.
         *
         * @return the result
         */

        public Date getFinishedAt() {
            return finishedAt;
        }

        /**
         * Sets the finished at.
         *
         * @param finishedAt the finished at value
         */

        public void setFinishedAt(Date finishedAt) {
            this.finishedAt = finishedAt;
        }

        /**
         * Returns the duration.
         *
         * @return the result
         */

        public Integer getDuration() {
            return duration;
        }

        /**
         * Sets the duration.
         *
         * @param duration the duration value
         */

        public void setDuration(Integer duration) {
            this.duration = duration;
        }

        /**
         * Returns the queued duration.
         *
         * @return the result
         */

        public Float getQueuedDuration() {
            return queuedDuration;
        }

        /**
         * Sets the queued duration.
         *
         * @param queuedDuration the queued duration value
         */

        public void setQueuedDuration(Float queuedDuration) {
            this.queuedDuration = queuedDuration;
        }

        /**
         * Returns the variables.
         *
         * @return the result
         */

        public List<Variable> getVariables() {
            return variables;
        }

        /**
         * Sets the variables.
         *
         * @param variables the variables value
         */

        public void setVariables(List<Variable> variables) {
            this.variables = variables;
        }

        /**
         * Returns the url.
         *
         * @return the result
         */

        public String getUrl() {
            return url;
        }

        /**
         * Sets the url.
         *
         * @param url the url value
         */

        public void setUrl(String url) {
            this.url = url;
        }

    }

}
