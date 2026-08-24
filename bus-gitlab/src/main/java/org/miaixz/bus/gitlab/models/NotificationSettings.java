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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumCodec;

/**
 * The notification settings class.
 *
 * @author Kimi Liu
 */
public class NotificationSettings implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852266179825L;
    /**
     * The level value.
     */
    private Level level;
    /**
     * The email value.
     */
    private String email;
    /**
     * The events value.
     */
    private Events events;

    /**
     * Constructs a new {@code NotificationSettings} instance.
     */
    public NotificationSettings() {
        // No initialization required.
    }

    /**
     * Returns the level.
     *
     * @return the result
     */

    public Level getLevel() {
        return level;
    }

    /**
     * Sets the level.
     *
     * @param level the level value
     */

    public void setLevel(Level level) {
        this.level = level;
    }

    /**
     * Returns the email.
     *
     * @return the result
     */

    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     *
     * @param email the email value
     */

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the events.
     *
     * @return the result
     */

    public Events getEvents() {
        return events;
    }

    /**
     * Sets the events.
     *
     * @param events the events value
     */

    public void setEvents(Events events) {
        this.events = events;
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
     * Notification level
     *
     * @author Kimi Liu
     */
    public static enum Level {

        /**
         * The disabled level.
         */
        DISABLED,
        /**
         * The participating level.
         */
        PARTICIPATING,
        /**
         * The watch level.
         */
        WATCH,
        /**
         * The global level.
         */
        GLOBAL,
        /**
         * The mention level.
         */
        MENTION,
        /**
         * The custom level.
         */
        CUSTOM;

        /**
         * The enum codec value.
         */
        private static JacksonJsonEnumCodec<Level> enumCodec = new JacksonJsonEnumCodec<>(Level.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static Level forValue(String value) {
            return enumCodec.forValue(value);
        }

        /**
         * Returns the value.
         *
         * @return the result
         */

        @JsonValue
        public String toValue() {
            return (enumCodec.toString(this));
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (enumCodec.toString(this));
        }

    }

    /**
     * The events class.
     *
     * @author Kimi Liu
     */
    public static class Events implements Serializable {

        /**
         * The serial version uid value.
         */
        @Serial
        private static final long serialVersionUID = 2852266282729L;

        /**
         * The new note value.
         */
        private Boolean newNote;
        /**
         * The new issue value.
         */
        private Boolean newIssue;
        /**
         * The reopen issue value.
         */
        private Boolean reopenIssue;
        /**
         * The close issue value.
         */
        private Boolean closeIssue;
        /**
         * The reassign issue value.
         */
        private Boolean reassignIssue;
        /**
         * The new merge request value.
         */
        private Boolean newMergeRequest;
        /**
         * The reopen merge request value.
         */
        private Boolean reopenMergeRequest;
        /**
         * The close merge request value.
         */
        private Boolean closeMergeRequest;
        /**
         * The reassign merge request value.
         */
        private Boolean reassignMergeRequest;
        /**
         * The merge merge request value.
         */
        private Boolean mergeMergeRequest;
        /**
         * The failed pipeline value.
         */
        private Boolean failedPipeline;
        /**
         * The success pipeline value.
         */
        private Boolean successPipeline;

        /**
         * Constructs a new {@code Events} instance.
         */
        public Events() {
            // No initialization required.
        }

        /**
         * Returns the new note.
         *
         * @return the result
         */

        public Boolean getNewNote() {
            return newNote;
        }

        /**
         * Sets the new note.
         *
         * @param newNote the new note value
         */

        public void setNewNote(Boolean newNote) {
            this.newNote = newNote;
        }

        /**
         * Returns the new issue.
         *
         * @return the result
         */

        public Boolean getNewIssue() {
            return newIssue;
        }

        /**
         * Sets the new issue.
         *
         * @param newIssue the new issue value
         */

        public void setNewIssue(Boolean newIssue) {
            this.newIssue = newIssue;
        }

        /**
         * Returns the reopen issue.
         *
         * @return the result
         */

        public Boolean getReopenIssue() {
            return reopenIssue;
        }

        /**
         * Sets the reopen issue.
         *
         * @param reopenIssue the reopen issue value
         */

        public void setReopenIssue(Boolean reopenIssue) {
            this.reopenIssue = reopenIssue;
        }

        /**
         * Returns the close issue.
         *
         * @return the result
         */

        public Boolean getCloseIssue() {
            return closeIssue;
        }

        /**
         * Sets the close issue.
         *
         * @param closeIssue the close issue value
         */

        public void setCloseIssue(Boolean closeIssue) {
            this.closeIssue = closeIssue;
        }

        /**
         * Returns the reassign issue.
         *
         * @return the result
         */

        public Boolean getReassignIssue() {
            return reassignIssue;
        }

        /**
         * Sets the reassign issue.
         *
         * @param reassignIssue the reassign issue value
         */

        public void setReassignIssue(Boolean reassignIssue) {
            this.reassignIssue = reassignIssue;
        }

        /**
         * Returns the new merge request.
         *
         * @return the result
         */

        public Boolean getNewMergeRequest() {
            return newMergeRequest;
        }

        /**
         * Sets the new merge request.
         *
         * @param newMergeRequest the new merge request value
         */

        public void setNewMergeRequest(Boolean newMergeRequest) {
            this.newMergeRequest = newMergeRequest;
        }

        /**
         * Returns the reopen merge request.
         *
         * @return the result
         */

        public Boolean getReopenMergeRequest() {
            return reopenMergeRequest;
        }

        /**
         * Sets the reopen merge request.
         *
         * @param reopenMergeRequest the reopen merge request value
         */

        public void setReopenMergeRequest(Boolean reopenMergeRequest) {
            this.reopenMergeRequest = reopenMergeRequest;
        }

        /**
         * Returns the close merge request.
         *
         * @return the result
         */

        public Boolean getCloseMergeRequest() {
            return closeMergeRequest;
        }

        /**
         * Sets the close merge request.
         *
         * @param closeMergeRequest the close merge request value
         */

        public void setCloseMergeRequest(Boolean closeMergeRequest) {
            this.closeMergeRequest = closeMergeRequest;
        }

        /**
         * Returns the reassign merge request.
         *
         * @return the result
         */

        public Boolean getReassignMergeRequest() {
            return reassignMergeRequest;
        }

        /**
         * Sets the reassign merge request.
         *
         * @param reassignMergeRequest the reassign merge request value
         */

        public void setReassignMergeRequest(Boolean reassignMergeRequest) {
            this.reassignMergeRequest = reassignMergeRequest;
        }

        /**
         * Returns the merge merge request.
         *
         * @return the result
         */

        public Boolean getMergeMergeRequest() {
            return mergeMergeRequest;
        }

        /**
         * Sets the merge merge request.
         *
         * @param mergeMergeRequest the merge merge request value
         */

        public void setMergeMergeRequest(Boolean mergeMergeRequest) {
            this.mergeMergeRequest = mergeMergeRequest;
        }

        /**
         * Returns the failed pipeline.
         *
         * @return the result
         */

        public Boolean getFailedPipeline() {
            return failedPipeline;
        }

        /**
         * Sets the failed pipeline.
         *
         * @param failedPipeline the failed pipeline value
         */

        public void setFailedPipeline(Boolean failedPipeline) {
            this.failedPipeline = failedPipeline;
        }

        /**
         * Returns the success pipeline.
         *
         * @return the result
         */

        public Boolean getSuccessPipeline() {
            return successPipeline;
        }

        /**
         * Sets the success pipeline.
         *
         * @param successPipeline the success pipeline value
         */

        public void setSuccessPipeline(Boolean successPipeline) {
            this.successPipeline = successPipeline;
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

}
