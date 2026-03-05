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
package org.miaixz.bus.storage;

import org.miaixz.bus.core.lang.Normal;

import lombok.*;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

/**
 * Represents the context for storage operations, containing configuration details for connecting to a storage service.
 * This class holds all necessary connection parameters and settings for storage providers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Context {

    /**
     * The URL prefix for the storage service.
     */
    @Builder.Default
    private String prefix = Normal.EMPTY;
    /**
     * The name of the bucket or container.
     */
    private String bucket;
    /**
     * The endpoint URL of the storage service.
     */
    private String endpoint;
    /**
     * The access key for authentication.
     */
    private String accessKey;
    /**
     * The secret key for authentication.
     */
    private String secretKey;
    /**
     * The region where the storage bucket is located.
     */
    private String region;

    /**
     * Extension properties for the storage context.
     */
    private String extension;

    /**
     * Indicates whether the connection is secure (e.g., using HTTPS).
     */
    private boolean secure;

    /**
     * Indicates whether path-style access is used for buckets. Default is {@code true}.
     */
    @Builder.Default
    private boolean pathStyle = true;
    /**
     * The connection timeout in seconds. Default is 30 seconds.
     */
    @Builder.Default
    private long connectTimeout = 30;
    /**
     * The write timeout in seconds. Default is 60 seconds.
     */
    @Builder.Default
    private long writeTimeout = 60;
    /**
     * The read timeout in seconds. Default is 30 seconds.
     */
    @Builder.Default
    private long readTimeout = 30;

}
