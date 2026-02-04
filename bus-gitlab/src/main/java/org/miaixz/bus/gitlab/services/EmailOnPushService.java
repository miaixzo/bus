/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab.services;

import org.miaixz.bus.gitlab.models.GitLabForm;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serial;

public class EmailOnPushService extends NotificationService {

    @Serial
    private static final long serialVersionUID = 2852283959961L;

    public static final String RECIPIENT_PROP = "recipients";
    public static final String DISABLE_DIFFS_PROP = "disable_diffs";
    public static final String SEND_FROM_COMMITTER_EMAIL_PROP = "send_from_committer_email";

    @Override
    public GitLabForm servicePropertiesForm() {
        GitLabForm formData = new GitLabForm().withParam(RECIPIENT_PROP, getRecipients(), true)
                .withParam(DISABLE_DIFFS_PROP, getDisableDiffs())
                .withParam(SEND_FROM_COMMITTER_EMAIL_PROP, getSendFromCommitterEmail())
                .withParam(PUSH_EVENTS_PROP, getPushEvents()).withParam("tag_push_events", getTagPushEvents())
                .withParam(BRANCHES_TO_BE_NOTIFIED_PROP, getBranchesToBeNotified());
        return formData;
    }

    public EmailOnPushService withPushEvents(Boolean pushEvents) {
        return withPushEvents(pushEvents, this);
    }

    public EmailOnPushService withTagPushEvents(Boolean pushEvents) {
        return withTagPushEvents(pushEvents, this);
    }

    @JsonIgnore
    public String getRecipients() {
        return (getProperty(RECIPIENT_PROP));
    }

    public void setRecipients(String recipients) {
        setProperty(RECIPIENT_PROP, recipients);
    }

    public EmailOnPushService withRecipients(String recipients) {
        setRecipients(recipients);
        return this;
    }

    @JsonIgnore
    public Boolean getDisableDiffs() {
        return Boolean.valueOf(getProperty(DISABLE_DIFFS_PROP, false));
    }

    public void setDisableDiffs(Boolean disableDiffs) {
        setProperty(DISABLE_DIFFS_PROP, disableDiffs);
    }

    public EmailOnPushService withDisableDiffs(Boolean disableDiffs) {
        setDisableDiffs(disableDiffs);
        return this;
    }

    @JsonIgnore
    public Boolean getSendFromCommitterEmail() {
        return Boolean.valueOf(getProperty(SEND_FROM_COMMITTER_EMAIL_PROP, false));
    }

    public void setSendFromCommitterEmail(Boolean sendFromCommitterEmail) {
        setProperty(SEND_FROM_COMMITTER_EMAIL_PROP, sendFromCommitterEmail);
    }

    public EmailOnPushService withSendFromCommitterEmail(Boolean sendFromCommitterEmail) {
        setSendFromCommitterEmail(sendFromCommitterEmail);
        return this;
    }

    @JsonIgnore
    public BranchesToBeNotified getBranchesToBeNotified() {
        String branchesToBeNotified = getProperty(BRANCHES_TO_BE_NOTIFIED_PROP);

        if (branchesToBeNotified == null || branchesToBeNotified.isEmpty()) {
            return null;
        }

        return (BranchesToBeNotified.valueOf(branchesToBeNotified.toUpperCase()));
    }

    public void setBranchesToBeNotified(BranchesToBeNotified branchesToBeNotified) {
        setProperty(BRANCHES_TO_BE_NOTIFIED_PROP, branchesToBeNotified.toString());
    }

    public EmailOnPushService withBranchesToBeNotified(BranchesToBeNotified branchesToBeNotified) {
        setBranchesToBeNotified(branchesToBeNotified);
        return this;
    }

}
