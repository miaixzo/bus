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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.miaixz.bus.gitlab.support.JacksonJson;

import java.io.Serializable;

public class Changes implements Serializable {
    private static final long serialVersionUID = -1L;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("a_mode")
    private String a_mode;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("b_mode")
    private String b_mode;

    private Boolean deletedFile;
    private String diff;
    private Boolean newFile;
    private String newPath;
    private String oldPath;
    private Boolean renamedFile;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("a_mode")
    public String getAMode() {
        return a_mode;
    }

    public void setAMode(String a_mode) {
        this.a_mode = a_mode;
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("b_mode")
    public String getBMode() {
        return b_mode;
    }

    public void setBMode(String b_mode) {
        this.b_mode = b_mode;
    }

    public Boolean getDeletedFile() {
        return deletedFile;
    }

    public void setDeletedFile(Boolean deletedFile) {
        this.deletedFile = deletedFile;
    }

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }

    public Boolean getNewFile() {
        return newFile;
    }

    public void setNewFile(Boolean newFile) {
        this.newFile = newFile;
    }

    public String getNewPath() {
        return newPath;
    }

    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    public Boolean getRenamedFile() {
        return renamedFile;
    }

    public void setRenamedFile(Boolean renamedFile) {
        this.renamedFile = renamedFile;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }
}
