package org.aoju.bus.gitlab.models;

import org.aoju.bus.gitlab.utils.JacksonJson;

import java.util.List;

public class ProtectedBranch {

    private String name;
    private List<org.aoju.bus.gitlab.models.BranchAccessLevel> pushAccessLevels;
    private List<org.aoju.bus.gitlab.models.BranchAccessLevel> mergeAccessLevels;

    public static final boolean isValid(ProtectedBranch branch) {
        return (branch != null && branch.getName() != null);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<org.aoju.bus.gitlab.models.BranchAccessLevel> getPushAccessLevels() {
        return this.pushAccessLevels;
    }

    public void setPushAccessLevels(List<org.aoju.bus.gitlab.models.BranchAccessLevel> pushAccessLevels) {
        this.pushAccessLevels = pushAccessLevels;
    }

    public List<org.aoju.bus.gitlab.models.BranchAccessLevel> getMergeAccessLevels() {
        return this.mergeAccessLevels;
    }

    public void setMergeAccessLevels(List<org.aoju.bus.gitlab.models.BranchAccessLevel> mergeAccessLevels) {
        this.mergeAccessLevels = mergeAccessLevels;
    }

    public ProtectedBranch withName(String name) {
        this.name = name;
        return this;
    }

    public ProtectedBranch withPushAccessLevels(List<org.aoju.bus.gitlab.models.BranchAccessLevel> pushAccessLevels) {
        this.pushAccessLevels = pushAccessLevels;
        return this;
    }

    public ProtectedBranch withMergeAccessLevels(List<BranchAccessLevel> mergeAccessLevels) {
        this.mergeAccessLevels = mergeAccessLevels;
        return this;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }
}
