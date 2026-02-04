/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.plugin.httpz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.io.InputStream;

/**
 * Represents a file or data source to be uploaded as part of a multipart request. It can hold content from a byte
 * array, a {@link File}, or an {@link InputStream}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MultipartFile {

    /**
     * The name of the file to be used in the 'filename' parameter of the 'Content-Disposition' header.
     */
    public String name;
    /**
     * The name of the multipart form field (the 'name' parameter of the 'Content-Disposition' header).
     */
    public String part;
    /**
     * The content of the file as a byte array. Use this for small files or in-memory data.
     */
    public byte[] content;
    /**
     * The {@link File} object to upload. Use this for uploading files from the filesystem.
     */
    public File file;
    /**
     * The {@link InputStream} providing the file content. Use this for streaming data. The stream will be fully read
     * during the request.
     */
    public InputStream in;

}
