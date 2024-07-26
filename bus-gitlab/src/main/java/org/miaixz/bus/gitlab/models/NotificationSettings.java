/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org gitlab4j and other contributors.           ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

import java.io.Serializable;

public class NotificationSettings implements Serializable {
    private static final long serialVersionUID = -1L;

    private Level level;

    public static class Events implements Serializable {
        private static final long serialVersionUID = -1L;

        private Boolean newNote;
        private Boolean newIssue;
        private Boolean reopenIssue;
        private Boolean closeIssue;
        private Boolean reassignIssue;
        private Boolean newMergeRequest;
        private Boolean reopenMergeRequest;
        private Boolean closeMergeRequest;
        private Boolean reassignMergeRequest;
        private Boolean mergeMergeRequest;
        private Boolean failedPipeline;
        private Boolean successPipeline;

        public Boolean getNewNote() {
            return newNote;
        }

        public void setNewNote(Boolean newNote) {
            this.newNote = newNote;
        }

        public Boolean getNewIssue() {
            return newIssue;
        }

        public void setNewIssue(Boolean newIssue) {
            this.newIssue = newIssue;
        }

        public Boolean getReopenIssue() {
            return reopenIssue;
        }

        public void setReopenIssue(Boolean reopenIssue) {
            this.reopenIssue = reopenIssue;
        }

        public Boolean getCloseIssue() {
            return closeIssue;
        }

        public void setCloseIssue(Boolean closeIssue) {
            this.closeIssue = closeIssue;
        }

        public Boolean getReassignIssue() {
            return reassignIssue;
        }

        public void setReassignIssue(Boolean reassignIssue) {
            this.reassignIssue = reassignIssue;
        }

        public Boolean getNewMergeRequest() {
            return newMergeRequest;
        }

        public void setNewMergeRequest(Boolean newMergeRequest) {
            this.newMergeRequest = newMergeRequest;
        }

        public Boolean getReopenMergeRequest() {
            return reopenMergeRequest;
        }

        public void setReopenMergeRequest(Boolean reopenMergeRequest) {
            this.reopenMergeRequest = reopenMergeRequest;
        }

        public Boolean getCloseMergeRequest() {
            return closeMergeRequest;
        }

        public void setCloseMergeRequest(Boolean closeMergeRequest) {
            this.closeMergeRequest = closeMergeRequest;
        }

        public Boolean getReassignMergeRequest() {
            return reassignMergeRequest;
        }

        public void setReassignMergeRequest(Boolean reassignMergeRequest) {
            this.reassignMergeRequest = reassignMergeRequest;
        }

        public Boolean getMergeMergeRequest() {
            return mergeMergeRequest;
        }

        public void setMergeMergeRequest(Boolean mergeMergeRequest) {
            this.mergeMergeRequest = mergeMergeRequest;
        }

        public Boolean getFailedPipeline() {
            return failedPipeline;
        }

        public void setFailedPipeline(Boolean failedPipeline) {
            this.failedPipeline = failedPipeline;
        }

        public Boolean getSuccessPipeline() {
            return successPipeline;
        }

        public void setSuccessPipeline(Boolean successPipeline) {
            this.successPipeline = successPipeline;
        }

        @Override
        public String toString() {
            return (JacksonJson.toJsonString(this));
        }
    }

    private String email;
    private Events events;

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Events getEvents() {
        return events;
    }

    public void setEvents(Events events) {
        this.events = events;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

    /**
     * Notification level
     */
    public static enum Level {

        DISABLED, PARTICIPATING, WATCH, GLOBAL, MENTION, CUSTOM;

        private static JacksonJsonEnumHelper<Level> enumHelper = new JacksonJsonEnumHelper<>(Level.class);

        @JsonCreator
        public static Level forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }
}
