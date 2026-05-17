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
package org.miaixz.bus.gitlab.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The push rules class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PushRules implements Serializable {

    /**
     * Serialization version identifier.
     */
    @Serial
    private static final long serialVersionUID = 2852276372752L;

    /**
     * Push rules identifier.
     */
    private Long id;

    /**
     * Project identifier associated with these push rules.
     */
    private Long projectId;

    /**
     * Regular expression that commit messages must match.
     */
    private String commitMessageRegex;

    /**
     * Regular expression that commit messages must not match.
     */
    private String commitMessageNegativeRegex;

    /**
     * Regular expression that branch names must match.
     */
    private String branchNameRegex;

    /**
     * Flag indicating whether tag deletion is denied.
     */
    private Boolean denyDeleteTag;

    /**
     * Time when the push rules were created.
     */
    private Date createdAt;

    /**
     * Flag indicating whether commit authors must be GitLab members.
     */
    private Boolean memberCheck;

    /**
     * Flag indicating whether GitLab should reject commits that may contain secrets.
     */
    private Boolean preventSecrets;

    /**
     * Regular expression that author email addresses must match.
     */
    private String authorEmailRegex;

    /**
     * Regular expression that file names must not match.
     */
    private String fileNameRegex;

    /**
     * Maximum file size, in megabytes.
     */
    private Integer maxFileSize;

    /**
     * Flag indicating whether commits must use a verified committer email address.
     */
    private Boolean commitCommitterCheck;

    /**
     * Flag indicating whether commit author names must match the GitLab account name.
     */
    private Boolean commitCommitterNameCheck;

    /**
     * Flag indicating whether unsigned commits are rejected.
     */
    private Boolean rejectUnsignedCommits;

    /**
     * Flag indicating whether commits without Developer Certificate of Origin sign-off are rejected.
     */
    private Boolean rejectNonDcoCommits;

    /**
     * Gets the push rules identifier.
     *
     * @return the push rules identifier
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the push rules identifier.
     *
     * @param id the push rules identifier
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the project identifier associated with these push rules.
     *
     * @return the project identifier
     */
    public Long getProjectId() {
        return projectId;
    }

    /**
     * Sets the project identifier associated with these push rules.
     *
     * @param projectId the project identifier
     */
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    /**
     * Sets the project identifier associated with these push rules.
     *
     * @param projectId the project identifier
     * @return the reference to this {@code PushRules} instance
     */
    public PushRules withProjectId(Long projectId) {
        this.projectId = projectId;
        return (this);
    }

    /**
     * Gets the regular expression that commit messages must match.
     *
     * @return the required commit message regular expression
     */
    public String getCommitMessageRegex() {
        return commitMessageRegex;
    }

    /**
     * Sets the regular expression that commit messages must match.
     *
     * @param commitMessageRegex the required commit message regular expression
     */
    public void setCommitMessageRegex(String commitMessageRegex) {
        this.commitMessageRegex = commitMessageRegex;
    }

    /**
     * Sets the regular expression that commit messages must match.
     *
     * @param commitMessageRegex the required commit message regular expression
     * @return the reference to this {@code PushRules} instance
     */
    public PushRules withCommitMessageRegex(String commitMessageRegex) {
        this.commitMessageRegex = commitMessageRegex;
        return (this);
    }

    /**
     * Gets the regular expression that commit messages must not match.
     *
     * @return the rejected commit message regular expression
     */
    public String getCommitMessageNegativeRegex() {
        return commitMessageNegativeRegex;
    }

    /**
     * Sets the regular expression that commit messages must not match.
     *
     * @param commitMessageNegativeRegex the rejected commit message regular expression
     */
    public void setCommitMessageNegativeRegex(String commitMessageNegativeRegex) {
        this.commitMessageNegativeRegex = commitMessageNegativeRegex;
    }

    /**
     * Sets the regular expression that commit messages must not match.
     *
     * @param commitMessageNegativeRegex the rejected commit message regular expression
     * @return the reference to this {@code PushRules} instance
     */
    public PushRules withCommitMessageNegativeRegex(String commitMessageNegativeRegex) {
        this.commitMessageNegativeRegex = commitMessageNegativeRegex;
        return (this);
    }

    /**
     * Gets the regular expression that branch names must match.
     *
     * @return the branch name regular expression
     */
    public String getBranchNameRegex() {
        return branchNameRegex;
    }

    /**
     * Sets the regular expression that branch names must match.
     *
     * @param branchNameRegex the branch name regular expression
     */
    public void setBranchNameRegex(String branchNameRegex) {
        this.branchNameRegex = branchNameRegex;
    }

    /**
     * Sets the regular expression that branch names must match.
     *
     * @param branchNameRegex the branch name regular expression
     * @return the reference to this {@code PushRules} instance
     */
    public PushRules withBranchNameRegex(String branchNameRegex) {
        this.branchNameRegex = branchNameRegex;
        return (this);
    }

    /**
     * Gets whether tag deletion is denied.
     *
     * @return {@code true} if tag deletion is denied
     */
    public Boolean getDenyDeleteTag() {
        return denyDeleteTag;
    }

    /**
     * Sets whether tag deletion is denied.
     *
     * @param denyDeleteTag {@code true} if tag deletion is denied
     */
    public void setDenyDeleteTag(Boolean denyDeleteTag) {
        this.denyDeleteTag = denyDeleteTag;
    }

    /**
     * Sets whether tag deletion is denied.
     *
     * @param denyDeleteTag {@code true} if tag deletion is denied
     * @return the reference to this {@code PushRules} instance
     */
    public PushRules withDenyDeleteTag(Boolean denyDeleteTag) {
        this.denyDeleteTag = denyDeleteTag;
        return (this);
    }

    /**
     * Gets the time when the push rules were created.
     *
     * @return the creation time
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the time when the push rules were created.
     *
     * @param createdAt the creation time
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets whether commit authors must be GitLab members.
     *
     * @return {@code true} if commit authors must be GitLab members
     */
    public Boolean getMemberCheck() {
        return memberCheck;
    }

    /**
     * Sets whether commit authors must be GitLab members.
     *
     * @param memberCheck {@code true} if commit authors must be GitLab members
     */
    public void setMemberCheck(Boolean memberCheck) {
        this.memberCheck = memberCheck;
    }

    /**
     * Sets whether commit authors must be GitLab members.
     *
     * @param memberCheck {@code true} if commit authors must be GitLab members
     * @return the reference to this {@code PushRules} instance
     */
    public PushRules withMemberCheck(Boolean memberCheck) {
        this.memberCheck = memberCheck;
        return (this);
    }

    /**
     * Gets whether GitLab should reject commits that may contain secrets.
     *
     * @return {@code true} if commits that may contain secrets are rejected
     */
    public Boolean getPreventSecrets() {
        return preventSecrets;
    }

    /**
     * Sets whether GitLab should reject commits that may contain secrets.
     *
     * @param preventSecrets {@code true} if commits that may contain secrets are rejected
     */
    public void setPreventSecrets(Boolean preventSecrets) {
        this.preventSecrets = preventSecrets;
    }

    /**
     * Sets whether GitLab should reject commits that may contain secrets.
     *
     * @param preventSecrets {@code true} if commits that may contain secrets are rejected
     * @return the reference to this {@code PushRules} instance
     */
    public PushRules withPreventSecrets(Boolean preventSecrets) {
        this.preventSecrets = preventSecrets;
        return (this);
    }

    /**
     * Gets the regular expression that author email addresses must match.
     *
     * @return the author email regular expression
     */
    public String getAuthorEmailRegex() {
        return authorEmailRegex;
    }

    /**
     * Sets the regular expression that author email addresses must match.
     *
     * @param authorEmailRegex the author email regular expression
     */
    public void setAuthorEmailRegex(String authorEmailRegex) {
        this.authorEmailRegex = authorEmailRegex;
    }

    /**
     * Sets the regular expression that author email addresses must match.
     *
     * @param authorEmailRegex the author email regular expression
     * @return the reference to this {@code PushRules} instance
     */
    public PushRules withAuthorEmailRegex(String authorEmailRegex) {
        this.authorEmailRegex = authorEmailRegex;
        return (this);
    }

    /**
     * Gets the regular expression that file names must not match.
     *
     * @return the rejected file name regular expression
     */
    public String getFileNameRegex() {
        return fileNameRegex;
    }

    /**
     * Sets the regular expression that file names must not match.
     *
     * @param fileNameRegex the rejected file name regular expression
     */
    public void setFileNameRegex(String fileNameRegex) {
        this.fileNameRegex = fileNameRegex;
    }

    /**
     * Sets the regular expression that file names must not match.
     *
     * @param fileNameRegex the rejected file name regular expression
     * @return the reference to this {@code PushRules} instance
     */
    public PushRules withFileNameRegex(String fileNameRegex) {
        this.fileNameRegex = fileNameRegex;
        return (this);
    }

    /**
     * Gets the maximum file size, in megabytes.
     *
     * @return the maximum file size
     */
    public Integer getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * Sets the maximum file size, in megabytes.
     *
     * @param maxFileSize the maximum file size
     */
    public void setMaxFileSize(Integer maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    /**
     * Sets the maximum file size, in megabytes.
     *
     * @param maxFileSize the maximum file size
     * @return the reference to this {@code PushRules} instance
     */
    public PushRules withMaxFileSize(Integer maxFileSize) {
        this.maxFileSize = maxFileSize;
        return (this);
    }

    /**
     * Gets whether commits must use a verified committer email address.
     *
     * @return {@code true} if commits must use a verified committer email address
     */
    public Boolean getCommitCommitterCheck() {
        return commitCommitterCheck;
    }

    /**
     * Gets whether commit author names must match the GitLab account name.
     *
     * @return {@code true} if commit author names must match the GitLab account name
     */
    public Boolean getCommitCommitterNameCheck() {
        return commitCommitterNameCheck;
    }

    /**
     * Sets whether commit author names must match the GitLab account name.
     *
     * @param commitCommitterNameCheck {@code true} if commit author names must match the GitLab account name
     */
    public void setCommitCommitterNameCheck(Boolean commitCommitterNameCheck) {
        this.commitCommitterNameCheck = commitCommitterNameCheck;
    }

    /**
     * Sets whether commit author names must match the GitLab account name.
     *
     * @param commitCommitterNameCheck {@code true} if commit author names must match the GitLab account name
     * @return the reference to this {@code PushRules} instance
     */
    public PushRules withCommitCommitterNameCheck(Boolean commitCommitterNameCheck) {
        this.commitCommitterNameCheck = commitCommitterNameCheck;
        return (this);
    }

    /**
     * Sets whether commits must use a verified committer email address.
     *
     * @param commitCommitterCheck {@code true} if commits must use a verified committer email address
     */
    public void setCommitCommitterCheck(Boolean commitCommitterCheck) {
        this.commitCommitterCheck = commitCommitterCheck;
    }

    /**
     * Sets whether commits must use a verified committer email address.
     *
     * @param commitCommitterCheck {@code true} if commits must use a verified committer email address
     * @return the reference to this {@code PushRules} instance
     */
    public PushRules withCommitCommitterCheck(Boolean commitCommitterCheck) {
        this.commitCommitterCheck = commitCommitterCheck;
        return (this);
    }

    /**
     * Gets whether unsigned commits are rejected.
     *
     * @return {@code true} if unsigned commits are rejected
     */
    public Boolean getRejectUnsignedCommits() {
        return rejectUnsignedCommits;
    }

    /**
     * Sets whether unsigned commits are rejected.
     *
     * @param rejectUnsignedCommits {@code true} if unsigned commits are rejected
     */
    public void setRejectUnsignedCommits(Boolean rejectUnsignedCommits) {
        this.rejectUnsignedCommits = rejectUnsignedCommits;
    }

    /**
     * Sets whether unsigned commits are rejected.
     *
     * @param rejectUnsignedCommits {@code true} if unsigned commits are rejected
     * @return the reference to this {@code PushRules} instance
     */
    public PushRules withRejectUnsignedCommits(Boolean rejectUnsignedCommits) {
        this.rejectUnsignedCommits = rejectUnsignedCommits;
        return (this);
    }

    /**
     * Gets whether commits without Developer Certificate of Origin sign-off are rejected.
     *
     * @return {@code true} if commits without Developer Certificate of Origin sign-off are rejected
     */
    public Boolean getRejectNonDcoCommits() {
        return rejectNonDcoCommits;
    }

    /**
     * Sets whether commits without Developer Certificate of Origin sign-off are rejected.
     *
     * @param rejectNonDcoCommits {@code true} if commits without Developer Certificate of Origin sign-off are rejected
     */
    public void setRejectNonDcoCommits(Boolean rejectNonDcoCommits) {
        this.rejectNonDcoCommits = rejectNonDcoCommits;
    }

    /**
     * Sets whether commits without Developer Certificate of Origin sign-off are rejected.
     *
     * @param rejectNonDcoCommits {@code true} if commits without Developer Certificate of Origin sign-off are rejected
     * @return the reference to this {@code PushRules} instance
     */
    public PushRules withRejectNonDcoCommits(Boolean rejectNonDcoCommits) {
        this.rejectNonDcoCommits = rejectNonDcoCommits;
        return (this);
    }

    /**
     * Returns these push rules as a JSON string.
     *
     * @return these push rules serialized as JSON
     */
    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
