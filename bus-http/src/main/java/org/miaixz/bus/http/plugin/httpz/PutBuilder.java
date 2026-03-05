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
package org.miaixz.bus.http.plugin.httpz;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.http.Httpd;
import org.miaixz.bus.http.bodys.MultipartBody;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * A builder for creating HTTP PUT requests using a fluent interface. It supports setting URL, parameters, headers, a
 * raw request body, and multipart file uploads.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PutBuilder extends RequestBuilder<PutBuilder> {

    /**
     * The raw request body string (e.g., for JSON or XML).
     */
    private String body;
    /**
     * A pre-built multipart body.
     */
    private MultipartBody multipartBody;
    /**
     * A list of files to be included in the multipart request.
     */
    private List<MultipartFile> list;

    /**
     * Constructs a new {@code PutBuilder}.
     *
     * @param httpd The {@link Httpd} client instance.
     */
    public PutBuilder(Httpd httpd) {
        super(httpd);
        list = new ArrayList<>();
    }

    @Override
    public RequestCall build() {
        return new PutRequest(url, tag, params, headers, list, body, multipartBody, id).build(httpd);
    }

    /**
     * Sets a raw string as the request body. This is typically used for sending content like JSON or XML. The
     * 'Content-Type' header should be set accordingly.
     *
     * @param body The raw string content for the request body.
     * @return This builder instance for chaining.
     */
    public PutBuilder body(String body) {
        this.body = body;
        return this;
    }

    /**
     * Sets a pre-constructed {@link MultipartBody}. This allows for advanced multipart request configuration.
     *
     * @param multipartBody The pre-built multipart body.
     * @return This builder instance for chaining.
     */
    public PutBuilder multipartBody(MultipartBody multipartBody) {
        this.multipartBody = multipartBody;
        return this;
    }

    /**
     * Adds a file to the multipart request from a byte array.
     *
     * @param partName The name of the form field.
     * @param fileName The name of the file.
     * @param content  The file content as a byte array.
     * @return This builder instance for chaining.
     */
    public PutBuilder addFile(String partName, String fileName, byte[] content) {
        MultipartFile multipartFile = new MultipartFile();
        multipartFile.part = partName;
        multipartFile.name = fileName;
        multipartFile.content = content;
        list.add(multipartFile);
        return this;
    }

    /**
     * Adds a file to the multipart request from a {@link File} object.
     *
     * @param partName The name of the form field.
     * @param fileName The name of the file.
     * @param file     The file to be uploaded.
     * @return This builder instance for chaining.
     */
    public PutBuilder addFile(String partName, String fileName, File file) {
        MultipartFile multipartFile = new MultipartFile();
        multipartFile.part = partName;
        multipartFile.name = fileName;
        multipartFile.file = file;
        list.add(multipartFile);
        return this;
    }

    /**
     * Adds a file to the multipart request from a string, using the default UTF-8 encoding.
     *
     * @param partName The name of the form field.
     * @param fileName The name of the file.
     * @param content  The string content of the file.
     * @return This builder instance for chaining.
     * @throws UnsupportedEncodingException if UTF-8 is not supported.
     */
    public PutBuilder addFile(String partName, String fileName, String content) throws UnsupportedEncodingException {
        return addFile(partName, fileName, content, Charset.DEFAULT_UTF_8);
    }

    /**
     * Adds a file to the multipart request from a string with a specified charset.
     *
     * @param partName    The name of the form field.
     * @param fileName    The name of the file.
     * @param content     The string content of the file.
     * @param charsetName The name of the charset to use for encoding.
     * @return This builder instance for chaining.
     * @throws UnsupportedEncodingException if the specified charset is not supported.
     */
    public PutBuilder addFile(String partName, String fileName, String content, String charsetName)
            throws UnsupportedEncodingException {
        return addFile(partName, fileName, content.getBytes(charsetName));
    }

    /**
     * An alias for {@link #addFile(String, String, byte[])}.
     *
     * @param partName    The name of the form field.
     * @param fileName    The name of the file.
     * @param content     The file content as a byte array.
     * @param charsetName This parameter is ignored.
     * @return This builder instance for chaining.
     */
    public PutBuilder addFile(String partName, String fileName, byte[] content, String charsetName) {
        return addFile(partName, fileName, content);
    }

}
