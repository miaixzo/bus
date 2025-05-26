/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org gitlab4j and other contributors.           ~
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

import java.io.Serializable;

import org.miaixz.bus.gitlab.support.JacksonJson;
import java.io.Serial;

public class ContainerExpirationPolicy implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852258707705L;

    private String cadence;
    private Boolean enabled;
    private Integer keepN;
    private String olderThan;
    private String nameRegex;
    private String nameRegexKeep;

    private String nextRunAt;

    public String getCadence() {
        return cadence;
    }

    public void setCadence(String cadence) {
        this.cadence = cadence;
    }

    public ContainerExpirationPolicy withCadence(String cadence) {
        this.cadence = cadence;
        return this;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public ContainerExpirationPolicy withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Integer getKeepN() {
        return keepN;
    }

    public void setKeepN(Integer keepN) {
        this.keepN = keepN;
    }

    public ContainerExpirationPolicy withKeepN(Integer keepN) {
        this.keepN = keepN;
        return this;
    }

    public String getOlderThan() {
        return olderThan;
    }

    public void setOlderThan(String olderThan) {
        this.olderThan = olderThan;
    }

    public ContainerExpirationPolicy withOlderThan(String olderThan) {
        this.olderThan = olderThan;
        return this;
    }

    public String getNameRegex() {
        return nameRegex;
    }

    public void setNameRegex(String nameRegex) {
        this.nameRegex = nameRegex;
    }

    public ContainerExpirationPolicy withNameRegex(String nameRegex) {
        this.nameRegex = nameRegex;
        return this;
    }

    public String getNameRegexKeep() {
        return nameRegexKeep;
    }

    public void setNameRegexKeep(String nameRegexKeep) {
        this.nameRegexKeep = nameRegexKeep;
    }

    public ContainerExpirationPolicy withNameRegexKeep(String nameRegexKeep) {
        this.nameRegexKeep = nameRegexKeep;
        return this;
    }

    public String getNextRunAt() {
        return nextRunAt;
    }

    public void setNextRunAt(String nextRunAt) {
        this.nextRunAt = nextRunAt;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
