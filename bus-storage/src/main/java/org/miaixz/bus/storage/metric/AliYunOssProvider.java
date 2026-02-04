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
package org.miaixz.bus.storage.metric;

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
