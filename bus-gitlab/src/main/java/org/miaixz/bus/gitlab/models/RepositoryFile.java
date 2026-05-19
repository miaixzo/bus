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
import java.util.Base64;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.miaixz.bus.gitlab.models.Constants.Encoding;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The repository file class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RepositoryFile implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852280333826L;

    private String fileName; // file name only, Ex. class.rb
    private String filePath; // full path to file. Ex. lib/class.rb
    private Integer size;
    private Encoding encoding;
    private String content;
    private String contentSha256;
    private String ref;
    private String blobId;
    private String commitId;
    private String lastCommitId;

    /**
     * Returns the file name.
     *
     * @return the result
     */

    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the file name.
     *
     * @param fileName the file name value
     */

    public void setFileName(String fileName) {
        this.fileName = fileName;
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
     * Returns the size.
     *
     * @return the result
     */

    public Integer getSize() {
        return size;
    }

    /**
     * Sets the size.
     *
     * @param size the size value
     */

    public void setSize(Integer size) {
        this.size = size;
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
     * Returns the content sha256.
     *
     * @return the result
     */

    public String getContentSha256() {
        return contentSha256;
    }

    /**
     * Sets the content sha256.
     *
     * @param contentSha256 the content sha256 value
     */

    public void setContentSha256(String contentSha256) {
        this.contentSha256 = contentSha256;
    }

    /**
     * Returns the ref.
     *
     * @return the result
     */

    public String getRef() {
        return ref;
    }

    /**
     * Sets the ref.
     *
     * @param ref the ref value
     */

    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * Returns the blob id.
     *
     * @return the result
     */

    public String getBlobId() {
        return blobId;
    }

    /**
     * Sets the blob id.
     *
     * @param blobId the blob id value
     */

    public void setBlobId(String blobId) {
        this.blobId = blobId;
    }

    /**
     * Returns the commit id.
     *
     * @return the result
     */

    public String getCommitId() {
        return commitId;
    }

    /**
     * Sets the commit id.
     *
     * @param commitId the commit id value
     */

    public void setCommitId(String commitId) {
        this.commitId = commitId;
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
     * Returns the content as a String, base64 decoding it if necessary. For binary files it is recommended to use
     * getDecodedContentAsBytes()
     *
     * @return the content as a String, base64 decoding it if necessary
     */
    @JsonIgnore
    public String getDecodedContentAsString() {

        if (content == null) {
            return (null);
        }

        if (Encoding.BASE64.equals(encoding)) {
            return (new String(Base64.getDecoder().decode(content)));
        }

        return (content);
    }

    /**
     * Returns the content as a byte array, decoding from base64 if necessary. For String content it is recommended to
     * use getDecodedContent().
     *
     * @return the content as a byte array, decoding from base64 if necessary
     */
    @JsonIgnore
    public byte[] getDecodedContentAsBytes() {

        if (content == null) {
            return (null);
        }

        if (encoding == Encoding.BASE64) {
            return (Base64.getDecoder().decode(content));
        }

        return (content.getBytes());
    }

    /**
     * Encodes the provided String using Base64 and sets it as the content. The encoding property of this instance will
     * be set to base64.
     *
     * @param content the String content to encode and set as the base64 encoded String content
     */
    @JsonIgnore
    public void encodeAndSetContent(String content) {
        encodeAndSetContent(content != null ? content.getBytes() : null);
    }

    /**
     * Encodes the provided byte array using Base64 and sets it as the content. The encoding property of this instance
     * will be set to base64.
     *
     * @param byteContent the byte[] content to encode and set as the base64 encoded String content
     */
    @JsonIgnore
    public void encodeAndSetContent(byte[] byteContent) {

        if (byteContent == null) {
            this.content = null;
            return;
        }

        this.content = Base64.getEncoder().encodeToString(byteContent);
        encoding = Encoding.BASE64;
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
