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
package org.miaixz.bus.storage.metric;

import org.miaixz.bus.storage.Context;

/**
 * Storage service provider for Volcengine TOS (Tinder Object Storage). This provider extends the generic S3 provider to
 * integrate with Volcengine TOS using S3-compatible APIs.
 * <p>
 * Volcengine TOS is ByteDance's cloud object storage service that provides S3-compatible APIs, making it fully
 * compatible with the GenericS3Provider implementation.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class VolcengineTosProvider extends GenericS3Provider {

    /**
     * Constructs a Volcengine TOS provider with the given context. Initializes the S3-compatible client and presigner
     * using the provided credentials and endpoint configuration.
     * <p>
     * This provider inherits all functionality from {@link GenericS3Provider}, including:
     * </p>
     * <ul>
     * <li>Byte array-based downloads supporting all file types (images, PDFs, DOCX, etc.)</li>
     * <li>Automatic resource management with try-with-resources</li>
     * <li>Memory-efficient streaming for large files</li>
     * <li>Proper error handling and logging</li>
     * </ul>
     *
     * @param context The storage context, containing endpoint, bucket, access key, secret key, region, and timeout
     *                configurations specific to Volcengine TOS.
     * @throws IllegalArgumentException If required context parameters are missing or invalid.
     */
    public VolcengineTosProvider(Context context) {
        super(context);
    }

}
