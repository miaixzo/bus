/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
