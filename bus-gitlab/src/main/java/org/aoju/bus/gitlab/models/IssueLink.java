package org.aoju.bus.gitlab.models;

import org.aoju.bus.gitlab.utils.JacksonJson;

public class IssueLink {

    private org.aoju.bus.gitlab.models.Issue sourceIssue;
    private org.aoju.bus.gitlab.models.Issue targetIssue;

    public org.aoju.bus.gitlab.models.Issue getSourceIssue() {
        return sourceIssue;
    }

    public void setSourceIssue(org.aoju.bus.gitlab.models.Issue sourceIssue) {
        this.sourceIssue = sourceIssue;
    }

    public org.aoju.bus.gitlab.models.Issue getTargetIssue() {
        return targetIssue;
    }

    public void setTargetIssue(Issue targetIssue) {
        this.targetIssue = targetIssue;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }
}
