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
package org.miaixz.bus.storage.magic;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents blob information, typically for attachments or files. This class encapsulates metadata and content for
 * file storage operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Blob {

    /**
     * Unique identifier for the blob.
     */
    public String key;

    /**
     * The name of the blob.
     */
    public String name;

    /**
     * An alias or alternative name for the blob.
     */
    public String alias;

    /**
     * The path where the blob is stored.
     */
    public String path;

    /**
     * The size of the blob.
     */
    public String size;

    /**
     * The type or format of the blob.
     */
    public String type;

    /**
     * The status of the blob.
     */
    public String status;

    /**
     * The owner of the blob.
     */
    public String owner;

    /**
     * The hash value of the blob, typically for integrity verification.
     */
    public String hash;

    /**
     * The URL for accessing the blob, possibly a thumbnail URL.
     */
    public String url;

    /**
     * The storage platform where the blob is hosted.
     */
    public String platform;

    /**
     * The actual byte content of the blob (the Blob data). Be cautious with this field as it can consume large amounts
     * of memory.
     */
    public byte[] bytes;

    /**
     * Extended fields for the file, stored as a map.
     */
    public Map<String, Object> extend;

}
