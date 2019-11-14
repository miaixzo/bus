package org.aoju.bus.gitlab.models;

import org.aoju.bus.gitlab.utils.JacksonJson;

import java.util.List;

public class CompareResults {

    private org.aoju.bus.gitlab.models.Commit commit;
    private List<org.aoju.bus.gitlab.models.Commit> commits;
    ;
    private List<org.aoju.bus.gitlab.models.Diff> diffs;
    private Boolean compareTimeout;
    private Boolean compareSameRef;

    public org.aoju.bus.gitlab.models.Commit getCommit() {
        return commit;
    }

    public void setCommit(org.aoju.bus.gitlab.models.Commit commit) {
        this.commit = commit;
    }

    public List<org.aoju.bus.gitlab.models.Commit> getCommits() {
        return commits;
    }

    public void setCommits(List<Commit> commits) {
        this.commits = commits;
    }

    public List<org.aoju.bus.gitlab.models.Diff> getDiffs() {
        return diffs;
    }

    public void setDiffs(List<Diff> diffs) {
        this.diffs = diffs;
    }

    public Boolean getCompareTimeout() {
        return compareTimeout;
    }

    public void setCompareTimeout(Boolean compareTimeout) {
        this.compareTimeout = compareTimeout;
    }

    public Boolean getCompareSameRef() {
        return compareSameRef;
    }

    public void setCompareSameRef(Boolean compareSameRef) {
        this.compareSameRef = compareSameRef;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }
}
