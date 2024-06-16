/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org Greg Messner and other contributors.       ~
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

import org.miaixz.bus.gitlab.support.JacksonJson;

import java.io.Serializable;
import java.util.List;

public class EventData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String after;
    private String before;
    private List<Commit> commits;
    private String ref;
    private Repository repository;
    private Integer totalCommitsCount;
    private Long userId;
    private String userName;

    public String getAfter() {
        return this.after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getBefore() {
        return this.before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public List<Commit> getCommits() {
        return this.commits;
    }

    public void setCommits(List<Commit> commits) {
        this.commits = commits;
    }

    public String getRef() {
        return this.ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Repository getRepository() {
        return this.repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public Integer getTotalCommitsCount() {
        return this.totalCommitsCount;
    }

    public void setTotalCommitsCount(Integer totalCommitsCount) {
        this.totalCommitsCount = totalCommitsCount;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public EventData withAfter(String after) {
        this.after = after;
        return this;
    }

    public EventData withBefore(String before) {
        this.before = before;
        return this;
    }

    public EventData withCommits(List<Commit> commits) {
        this.commits = commits;
        return this;
    }

    public EventData withRef(String ref) {
        this.ref = ref;
        return this;
    }

    public EventData withRepository(Repository repository) {
        this.repository = repository;
        return this;
    }

    public EventData withTotalCommitsCount(Integer totalCommitsCount) {
        this.totalCommitsCount = totalCommitsCount;
        return this;
    }

    public EventData withUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public EventData withUserName(String userName) {
        this.userName = userName;
        return this;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }
}
