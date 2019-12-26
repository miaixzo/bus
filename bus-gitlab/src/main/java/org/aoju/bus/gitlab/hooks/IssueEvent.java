package org.aoju.bus.gitlab.hooks;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.gitlab.models.Assignee;
import org.aoju.bus.gitlab.models.User;
import org.aoju.bus.gitlab.utils.JacksonJson;

import java.util.List;

public class IssueEvent extends AbstractEvent {

    public static final String X_GITLAB_EVENT = "Issue Hook";
    public static final String OBJECT_KIND = "issue";

    private User user;
    private EventProject project;
    private EventRepository repository;
    private ObjectAttributes objectAttributes;
    private List<Assignee> assignees;
    private Assignee assignee;
    private List<EventLabel> labels;
    private IssueChanges changes;

    public String getObjectKind() {
        return (OBJECT_KIND);
    }

    public void setObjectKind(String objectKind) {
        if (!OBJECT_KIND.equals(objectKind))
            throw new RuntimeException("Invalid object_kind (" + objectKind + "), must be '" + OBJECT_KIND + Symbol.SINGLE_QUOTE);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public EventProject getProject() {
        return project;
    }

    public void setProject(EventProject project) {
        this.project = project;
    }

    public EventRepository getRepository() {
        return repository;
    }

    public void setRepository(EventRepository repository) {
        this.repository = repository;
    }

    public List<Assignee> getAssignees() {
        return assignees;
    }

    public void setAssignees(List<Assignee> assignees) {
        this.assignees = assignees;
    }

    public Assignee getAssignee() {
        return assignee;
    }

    public void setAssignee(Assignee assignee) {
        this.assignee = assignee;
    }

    public List<EventLabel> getLabels() {
        return labels;
    }

    public void setLabels(List<EventLabel> labels) {
        this.labels = labels;
    }

    public IssueChanges getChanges() {
        return changes;
    }

    public void setChanges(IssueChanges changes) {
        this.changes = changes;
    }

    public ObjectAttributes getObjectAttributes() {
        return this.objectAttributes;
    }

    public void setObjectAttributes(ObjectAttributes objectAttributes) {
        this.objectAttributes = objectAttributes;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

    public static class ObjectAttributes extends EventIssue {
    }
}
