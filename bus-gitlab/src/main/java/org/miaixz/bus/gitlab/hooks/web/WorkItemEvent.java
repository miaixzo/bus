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
import java.util.List;

import org.miaixz.bus.gitlab.models.User;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The work item event class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class WorkItemEvent extends AbstractEvent {

    @Serial
    private static final long serialVersionUID = 2852233532039L;
    /**
     * The x gitlab event value.
     */

    public static final String X_GITLAB_EVENT = "Issue Hook";
    /**
     * The object kind value.
     */
    public static final String OBJECT_KIND = "work_item";

    private User user;
    private EventProject project;
    private EventRepository repository;
    private ObjectAttributes objectAttributes;
    private List<EventLabel> labels;
    private WorkItemChanges changes;

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
     * Returns the user.
     *
     * @return the result
     */

    public User getUser() {
        return user;
    }

    /**
     * Sets the user.
     *
     * @param user the user value
     */

    public void setUser(User user) {
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

    public WorkItemChanges getChanges() {
        return changes;
    }

    /**
     * Sets the changes.
     *
     * @param changes the changes value
     */

    public void setChanges(WorkItemChanges changes) {
        this.changes = changes;
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
    public static class ObjectAttributes extends EventWorkItem {

    }

}
