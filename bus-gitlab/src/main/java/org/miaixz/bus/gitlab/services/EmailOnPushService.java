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
package org.miaixz.bus.gitlab.services;

import java.io.Serial;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.miaixz.bus.gitlab.models.GitLabForm;

/**
 * The email on push service class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EmailOnPushService extends NotificationService {

    @Serial
    private static final long serialVersionUID = 2852283959961L;
    /**
     * The recipient prop value.
     */

    public static final String RECIPIENT_PROP = "recipients";
    /**
     * The disable diffs prop value.
     */
    public static final String DISABLE_DIFFS_PROP = "disable_diffs";
    /**
     * The send from committer email prop value.
     */
    public static final String SEND_FROM_COMMITTER_EMAIL_PROP = "send_from_committer_email";

    /**
     * Executes the service properties form operation.
     *
     * @return the result
     */

    @Override
    public GitLabForm servicePropertiesForm() {
        GitLabForm formData = new GitLabForm().withParam(RECIPIENT_PROP, getRecipients(), true)
                .withParam(DISABLE_DIFFS_PROP, getDisableDiffs())
                .withParam(SEND_FROM_COMMITTER_EMAIL_PROP, getSendFromCommitterEmail())
                .withParam(PUSH_EVENTS_PROP, getPushEvents()).withParam("tag_push_events", getTagPushEvents())
                .withParam(BRANCHES_TO_BE_NOTIFIED_PROP, getBranchesToBeNotified());
        return formData;
    }

    /**
     * Sets the push events and returns this instance.
     *
     * @param pushEvents the push events value
     * @return the result
     */

    public EmailOnPushService withPushEvents(Boolean pushEvents) {
        return withPushEvents(pushEvents, this);
    }

    /**
     * Sets the tag push events and returns this instance.
     *
     * @param pushEvents the push events value
     * @return the result
     */

    public EmailOnPushService withTagPushEvents(Boolean pushEvents) {
        return withTagPushEvents(pushEvents, this);
    }

    /**
     * Returns the recipients.
     *
     * @return the result
     */

    @JsonIgnore
    public String getRecipients() {
        return (getProperty(RECIPIENT_PROP));
    }

    /**
     * Sets the recipients.
     *
     * @param recipients the recipients value
     */

    public void setRecipients(String recipients) {
        setProperty(RECIPIENT_PROP, recipients);
    }

    /**
     * Sets the recipients and returns this instance.
     *
     * @param recipients the recipients value
     * @return the result
     */

    public EmailOnPushService withRecipients(String recipients) {
        setRecipients(recipients);
        return this;
    }

    /**
     * Returns the disable diffs.
     *
     * @return the result
     */

    @JsonIgnore
    public Boolean getDisableDiffs() {
        return Boolean.valueOf(getProperty(DISABLE_DIFFS_PROP, false));
    }

    /**
     * Sets the disable diffs.
     *
     * @param disableDiffs the disable diffs value
     */

    public void setDisableDiffs(Boolean disableDiffs) {
        setProperty(DISABLE_DIFFS_PROP, disableDiffs);
    }

    /**
     * Sets the disable diffs and returns this instance.
     *
     * @param disableDiffs the disable diffs value
     * @return the result
     */

    public EmailOnPushService withDisableDiffs(Boolean disableDiffs) {
        setDisableDiffs(disableDiffs);
        return this;
    }

    /**
     * Returns the send from committer email.
     *
     * @return the result
     */

    @JsonIgnore
    public Boolean getSendFromCommitterEmail() {
        return Boolean.valueOf(getProperty(SEND_FROM_COMMITTER_EMAIL_PROP, false));
    }

    /**
     * Sets the send from committer email.
     *
     * @param sendFromCommitterEmail the send from committer email value
     */

    public void setSendFromCommitterEmail(Boolean sendFromCommitterEmail) {
        setProperty(SEND_FROM_COMMITTER_EMAIL_PROP, sendFromCommitterEmail);
    }

    /**
     * Sets the send from committer email and returns this instance.
     *
     * @param sendFromCommitterEmail the send from committer email value
     * @return the result
     */

    public EmailOnPushService withSendFromCommitterEmail(Boolean sendFromCommitterEmail) {
        setSendFromCommitterEmail(sendFromCommitterEmail);
        return this;
    }

    /**
     * Returns the branches to be notified.
     *
     * @return the result
     */

    @JsonIgnore
    public BranchesToBeNotified getBranchesToBeNotified() {
        String branchesToBeNotified = getProperty(BRANCHES_TO_BE_NOTIFIED_PROP);

        if (branchesToBeNotified == null || branchesToBeNotified.isEmpty()) {
            return null;
        }

        return (BranchesToBeNotified.valueOf(branchesToBeNotified.toUpperCase()));
    }

    /**
     * Sets the branches to be notified.
     *
     * @param branchesToBeNotified the branches to be notified value
     */

    public void setBranchesToBeNotified(BranchesToBeNotified branchesToBeNotified) {
        setProperty(BRANCHES_TO_BE_NOTIFIED_PROP, branchesToBeNotified.toString());
    }

    /**
     * Sets the branches to be notified and returns this instance.
     *
     * @param branchesToBeNotified the branches to be notified value
     * @return the result
     */

    public EmailOnPushService withBranchesToBeNotified(BranchesToBeNotified branchesToBeNotified) {
        setBranchesToBeNotified(branchesToBeNotified);
        return this;
    }

}
