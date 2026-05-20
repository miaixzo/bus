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
import java.io.Serializable;
import java.util.List;

import org.miaixz.bus.gitlab.hooks.web.MergeRequestEvent.ObjectAttributes;
import org.miaixz.bus.gitlab.models.Assignee;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The external status check event class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ExternalStatusCheckEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852232138157L;

    private String objectKind;
    private String eventType;
    private EventUser user;
    private EventProject project;
    private EventRepository repository;
    private ObjectAttributes objectAttributes;
    private List<EventLabel> labels;
    private MergeRequestChanges changes;
    private List<Assignee> assignees;
    private EventExternalStatusCheck externalApprovalRule;

    /**
     * Returns the object kind.
     *
     * @return the result
     */

    public String getObjectKind() {
        return objectKind;
    }

    /**
     * Sets the object kind.
     *
     * @param objectKind the object kind value
     */

    public void setObjectKind(String objectKind) {
        this.objectKind = objectKind;
    }

    /**
     * Returns the event type.
     *
     * @return the result
     */

    public String getEventType() {
        return eventType;
    }

    /**
     * Sets the event type.
     *
     * @param eventType the event type value
     */

    public void setEventType(String eventType) {
        this.eventType = eventType;
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
     * Returns the repository.
     *
     * @return the result
     */

    public EventRepository getRepository() {
        return repository;
    }

    /**
     * Sets the repository.
     *
     * @param repository the repository value
     */

    public void setRepository(EventRepository repository) {
        this.repository = repository;
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
     * Returns the labels.
     *
     * @return the result
     */

    public List<EventLabel> getLabels() {
        return labels;
    }

    /**
     * Sets the labels.
     *
     * @param labels the labels value
     */

    public void setLabels(List<EventLabel> labels) {
        this.labels = labels;
    }

    /**
     * Returns the changes.
     *
     * @return the result
     */

    public MergeRequestChanges getChanges() {
        return changes;
    }

    /**
     * Sets the changes.
     *
     * @param changes the changes value
     */

    public void setChanges(MergeRequestChanges changes) {
        this.changes = changes;
    }

    /**
     * Returns the assignees.
     *
     * @return the result
     */

    public List<Assignee> getAssignees() {
        return assignees;
    }

    /**
     * Sets the assignees.
     *
     * @param assignees the assignees value
     */

    public void setAssignees(List<Assignee> assignees) {
        this.assignees = assignees;
    }

    /**
     * Returns the external approval rule.
     *
     * @return the result
     */

    public EventExternalStatusCheck getExternalApprovalRule() {
        return externalApprovalRule;
    }

    /**
     * Sets the external approval rule.
     *
     * @param externalApprovalRule the external approval rule value
     */

    public void setExternalApprovalRule(EventExternalStatusCheck externalApprovalRule) {
        this.externalApprovalRule = externalApprovalRule;
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
