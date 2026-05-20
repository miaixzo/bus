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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Base64;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.models.Constants.Encoding;
import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;
import org.miaixz.bus.logger.Logger;

/**
 * The commit action class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CommitAction implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852239693533L;

    /**
     * Sets the file content and returns this instance.
     *
     * @param filePath the file path value
     * @param encoding the encoding value
     * @return the result
     */

    public CommitAction withFileContent(String filePath, Encoding encoding) {
        File file = new File(filePath);
        return (withFileContent(file, filePath, encoding));
    }

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

    /**
     * Returns the action.
     *
     * @return the result
     */

    public Action getAction() {
        return action;
    }

    /**
     * Sets the action.
     *
     * @param action the action value
     */

    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * Sets the action and returns this instance.
     *
     * @param action the action value
     * @return the result
     */

    public CommitAction withAction(Action action) {
        this.action = action;
        return this;
    }

    /**
     * Returns the file path.
     *
     * @return the result
     */

    public String getFilePath() {
        return filePath;
    }

    /**
     * Sets the file path.
     *
     * @param filePath the file path value
     */

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Sets the file path and returns this instance.
     *
     * @param filePath the file path value
     * @return the result
     */

    public CommitAction withFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    /**
     * Returns the previous path.
     *
     * @return the result
     */

    public String getPreviousPath() {
        return previousPath;
    }

    /**
     * Sets the previous path.
     *
     * @param previousPath the previous path value
     */

    public void setPreviousPath(String previousPath) {
        this.previousPath = previousPath;
    }

    /**
     * Sets the previous path and returns this instance.
     *
     * @param previousPath the previous path value
     * @return the result
     */

    public CommitAction withPreviousPath(String previousPath) {
        this.previousPath = previousPath;
        return this;
    }

    /**
     * Returns the content.
     *
     * @return the result
     */

    public String getContent() {
        return content;
    }

    /**
     * Sets the content.
     *
     * @param content the content value
     */

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Sets the content and returns this instance.
     *
     * @param content the content value
     * @return the result
     */

    public CommitAction withContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * Returns the encoding.
     *
     * @return the result
     */

    public Encoding getEncoding() {
        return encoding;
    }

    /**
     * Sets the encoding.
     *
     * @param encoding the encoding value
     */

    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    /**
     * Sets the encoding and returns this instance.
     *
     * @param encoding the encoding value
     * @return the result
     */

    public CommitAction withEncoding(Encoding encoding) {
        this.encoding = encoding;
        return this;
    }

    /**
     * Returns the last commit id.
     *
     * @return the result
     */

    public String getLastCommitId() {
        return lastCommitId;
    }

    /**
     * Sets the last commit id.
     *
     * @param lastCommitId the last commit id value
     */

    public void setLastCommitId(String lastCommitId) {
        this.lastCommitId = lastCommitId;
    }

    /**
     * Sets the last commit id and returns this instance.
     *
     * @param lastCommitId the last commit id value
     * @return the result
     */

    public CommitAction withLastCommitId(String lastCommitId) {
        this.lastCommitId = lastCommitId;
        return this;
    }

    /**
     * Returns the execute filemode.
     *
     * @return the result
     */

    public Boolean getExecuteFilemode() {
        return executeFilemode;
    }

    /**
     * Sets the execute filemode.
     *
     * @param executeFilemode the execute filemode value
     */

    public void setExecuteFilemode(Boolean executeFilemode) {
        this.executeFilemode = executeFilemode;
    }

    /**
     * Sets the execute filemode and returns this instance.
     *
     * @param executeFilemode the execute filemode value
     * @return the result
     */

    public CommitAction withExecuteFilemode(Boolean executeFilemode) {
        this.executeFilemode = executeFilemode;
        return this;
    }

    /**
     * Sets the file content and returns this instance.
     *
     * @param file     the file value
     * @param filePath the file path value
     * @param encoding the encoding value
     * @return the result
     */

    public CommitAction withFileContent(File file, String filePath, Encoding encoding) {

        this.encoding = (encoding != null ? encoding : Encoding.TEXT);
        this.filePath = filePath;

        try {
            content = getFileContentAsString(file, this.encoding);
        } catch (IOException e) {
            Logger.warn(
                    false,
                    "GitLab",
                    e,
                    "GitLab commit action file content read failed: fileNamePresent={}, filePathLength={}, encoding={}, exception={}",
                    file != null && file.getName() != null,
                    filePath == null ? -1 : filePath.length(),
                    this.encoding,
                    e.getClass().getSimpleName());
            throw new IllegalStateException(e);
        }

        return (this);
    }

    /**
     * The action enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Action {

        /**
         * The create action.
         */
        CREATE,
        /**
         * The delete action.
         */
        DELETE,
        /**
         * The move action.
         */
        MOVE,
        /**
         * The update action.
         */
        UPDATE,
        /**
         * The chmod action.
         */
        CHMOD;

        private static JacksonJsonEnumHelper<Action> enumHelper = new JacksonJsonEnumHelper<>(Action.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static Action forValue(String value) {
            return enumHelper.forValue(value);
        }

        /**
         * Returns the value.
         *
         * @return the result
         */

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }

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
