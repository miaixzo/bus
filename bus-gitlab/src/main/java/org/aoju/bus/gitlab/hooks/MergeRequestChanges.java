package org.aoju.bus.gitlab.hooks;

public class MergeRequestChanges extends EventChanges {

    private ChangeContainer<String> mergeStatus;

    public ChangeContainer<String> getMergeStatus() {
        return mergeStatus;
    }

    public void setMergeStatus(ChangeContainer<String> mergeStatus) {
        this.mergeStatus = mergeStatus;
    }
}
