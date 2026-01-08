/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.storage;

import org.miaixz.bus.core.lang.Normal;

import lombok.*;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

/**
 * Represents the context for storage operations, containing configuration details for connecting to a storage service.
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
