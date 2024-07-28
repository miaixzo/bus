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
import org.miaixz.bus.gitlab.Constants;
import org.miaixz.bus.gitlab.Constants.Encoding;
import org.miaixz.bus.gitlab.GitLabApiException;
import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Base64;

public class CommitAction implements Serializable {
    private static final long serialVersionUID = -1L;

    private Action action;
    private String filePath;
    private String previousPath;
    private String content;
    private Encoding encoding;
    private String lastCommitId;
    private Boolean executeFilemode;

    /**
     * Reads the content of a File instance and returns it as a String of either text or base64 encoded text.
     *
     * @param file     the File instance to read from
     * @param encoding whether to encode as Base64 or as Text, defaults to Text if null
     * @return the content of the File as a String
     * @throws IOException if any error occurs
     */
    public static String getFileContentAsString(File file, Constants.Encoding encoding) throws IOException {

        if (encoding == Constants.Encoding.BASE64) {

            try (FileInputStream stream = new FileInputStream(file)) {
                byte data[] = new byte[(int) file.length()];
                stream.read(data);
                return (Base64.getEncoder().encodeToString(data));
            }

        } else {
            return (new String(Files.readAllBytes(file.toPath())));
        }
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public CommitAction withAction(Action action) {
        this.action = action;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public CommitAction withFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public String getPreviousPath() {
        return previousPath;
    }

    public void setPreviousPath(String previousPath) {
        this.previousPath = previousPath;
    }

    public CommitAction withPreviousPath(String previousPath) {
        this.previousPath = previousPath;
        return this;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public CommitAction withContent(String content) {
        this.content = content;
        return this;
    }

    public Encoding getEncoding() {
        return encoding;
    }

    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    public CommitAction withEncoding(Encoding encoding) {
        this.encoding = encoding;
        return this;
    }

    public String getLastCommitId() {
        return lastCommitId;
    }

    public void setLastCommitId(String lastCommitId) {
        this.lastCommitId = lastCommitId;
    }

    public CommitAction withLastCommitId(String lastCommitId) {
        this.lastCommitId = lastCommitId;
        return this;
    }

    public Boolean getExecuteFilemode() {
        return executeFilemode;
    }

    public void setExecuteFilemode(Boolean executeFilemode) {
        this.executeFilemode = executeFilemode;
    }

    public CommitAction withExecuteFilemode(Boolean executeFilemode) {
        this.executeFilemode = executeFilemode;
        return this;
    }

    public CommitAction withFileContent(String filePath, Encoding encoding) throws GitLabApiException {
        File file = new File(filePath);
        return (withFileContent(file, filePath, encoding));
    }

    public CommitAction withFileContent(File file, String filePath, Encoding encoding) throws GitLabApiException {

        this.encoding = (encoding != null ? encoding : Encoding.TEXT);
        this.filePath = filePath;

        try {
            content = getFileContentAsString(file, this.encoding);
        } catch (IOException e) {
            throw new GitLabApiException(e);
        }

        return (this);
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

    public enum Action {

        CREATE, DELETE, MOVE, UPDATE, CHMOD;

        private static JacksonJsonEnumHelper<Action> enumHelper = new JacksonJsonEnumHelper<>(Action.class);

        @JsonCreator
        public static Action forValue(String value) {
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
