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
package org.miaixz.bus.storage.metric;

import java.io.*;
import java.net.URI;
import java.time.Duration;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.storage.ClientX;
import org.miaixz.bus.storage.Context;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Storage service provider for Alibaba Cloud Object Storage Service (OSS). This provider integrates with Alibaba Cloud
 * OSS using an S3-compatible client.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AliYunOssProvider extends GenericS3Provider {

    /**
     * Constructs an Alibaba Cloud OSS provider with the given context. Initializes the S3 client and presigner using
     * the provided credentials and endpoint configuration.
     *
     * @param context The storage context, containing endpoint, bucket, access key, secret key, and other
     *                configurations.
     * @throws IllegalArgumentException If required context parameters are missing or invalid.
     */
    public AliYunOssProvider(Context context) {
        super(context);
        this.context = context;

        Assert.notBlank(this.context.getEndpoint(), "[endpoint] cannot be blank");
        Assert.notBlank(this.context.getBucket(), "[bucket] cannot be blank");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] cannot be blank");
        Assert.notBlank(this.context.getSecretKey(), "[secretKey] cannot be blank");

        ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                .apiCallTimeout(Duration.ofSeconds(this.context.getWriteTimeout()))
                .apiCallAttemptTimeout(Duration.ofSeconds(this.context.getReadTimeout())).build();

        AwsBasicCredentials credentials = AwsBasicCredentials
                .create(this.context.getAccessKey(), this.context.getSecretKey());

        // Create custom ClientX
        ClientX clientx = new ClientX.ClientBuilder()
                .connectTimeout(Duration.ofSeconds(this.context.getConnectTimeout()))
                .readTimeout(Duration.ofSeconds(this.context.getReadTimeout()))
                .writeTimeout(Duration.ofSeconds(this.context.getWriteTimeout())).addInterceptor(chain -> {
                    Request request = chain.request();
                    return chain.proceed(request);
                }).build();

        // Configure S3 client for Alibaba Cloud OSS compatibility
        S3Configuration s3Config = S3Configuration.builder().pathStyleAccessEnabled(this.context.isPathStyle())
                // Disable chunked encoding to resolve Alibaba Cloud OSS compatibility issues
                .chunkedEncodingEnabled(false).build();

        this.client = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(credentials))
                .httpClient(clientx).endpointOverride(URI.create(this.context.getEndpoint()))
                .region(Region.of(StringKit.isBlank(this.context.getRegion()) ? "us-east-1" : this.context.getRegion()))
                .overrideConfiguration(overrideConfig).serviceConfiguration(s3Config).build();

        this.presigner = S3Presigner.builder().credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(this.context.getEndpoint()))
                .region(Region.of(StringKit.isBlank(this.context.getRegion()) ? "us-east-1" : this.context.getRegion()))
                .serviceConfiguration(
                        S3Configuration.builder().pathStyleAccessEnabled(this.context.isPathStyle())
                                .chunkedEncodingEnabled(false).build())
                .build();
    }

}
