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

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.ErrorCode;
import org.miaixz.bus.storage.magic.Material;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * 存储服务-京东云
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdOssProvider extends AbstractProvider {

    private volatile S3Client client;
    private volatile S3Presigner presigner;

    public JdOssProvider(Context context) {
        this.context = context;

        Assert.notBlank(this.context.getEndpoint(), "[endpoint] not defined");
        Assert.notBlank(this.context.getBucket(), "[bucket] not defined");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] not defined");
        Assert.notBlank(this.context.getSecretKey(), "[secretKey] not defined");
        Assert.notBlank(this.context.getRegion(), "[region] not defined");

        S3ClientBuilder builder = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsBasicCredentials.create(this.context.getAccessKey(), this.context.getSecretKey())))
                .region(Region.of(this.context.getRegion())).forcePathStyle(true); // 京东云需要路径风格的 URL

        S3Presigner.Builder presignerBuilder = S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsBasicCredentials.create(this.context.getAccessKey(), this.context.getSecretKey())))
                .region(Region.of(this.context.getRegion()));

        if (StringKit.isNotBlank(this.context.getEndpoint())) {
            builder.endpointOverride(java.net.URI.create(this.context.getEndpoint()));
            presignerBuilder.endpointOverride(java.net.URI.create(this.context.getEndpoint()));
        }

        this.client = builder.build();
        this.presigner = presignerBuilder.build();
    }

    @Override
    public Message download(String fileName) {
        return download(this.context.getBucket(), fileName);
    }

    @Override
    public Message download(String bucket, String fileName) {
        try {
            GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(fileName).build();
            InputStream inputStream = client.getObject(request);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).errmsg(ErrorCode.SUCCESS.getDesc())
                    .data(bufferedReader).build();
        } catch (SdkException e) {
            Logger.error("file download failed: {}", e.getMessage());
            return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
        }
    }

    @Override
    public Message download(String fileName, File file) {
        return download(this.context.getBucket(), fileName, file);
    }

    @Override
    public Message download(String bucket, String fileName, File file) {
        try {
            GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(fileName).build();
            client.getObject(request, file.toPath());
            return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).errmsg(ErrorCode.SUCCESS.getDesc()).build();
        } catch (SdkException e) {
            Logger.error("file download failed: {}", e.getMessage());
            return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
        }
    }

    @Override
    public Message list() {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(this.context.getBucket()).build();
            ListObjectsV2Response response = client.listObjectsV2(request);

            return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).errmsg(ErrorCode.SUCCESS.getDesc())
                    .data(response.contents().stream().map(item -> {
                        Map<String, Object> extend = new HashMap<>();
                        extend.put("tag", item.eTag());
                        extend.put("storageClass", item.storageClassAsString());
                        extend.put("lastModified", item.lastModified());
                        return Material.builder().name(item.key())
                                .owner(item.owner() != null ? item.owner().displayName() : null)
                                .size(StringKit.toString(item.size())).extend(extend).build();
                    }).collect(Collectors.toList())).build();
        } catch (SdkException e) {
            Logger.error("list objects failed: {}", e.getMessage());
            return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
        }
    }

    @Override
    public Message rename(String oldName, String newName) {
        return rename(this.context.getBucket(), oldName, newName);
    }

    @Override
    public Message rename(String bucket, String oldName, String newName) {
        return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg("failure to provide services").build();
    }

    @Override
    public Message upload(String fileName, byte[] content) {
        return upload(this.context.getBucket(), fileName, content);
    }

    @Override
    public Message upload(String bucket, String fileName, InputStream content) {
        try {
            PutObjectRequest request = PutObjectRequest.builder().bucket(bucket).key(fileName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM).build();
            client.putObject(request, RequestBody.fromInputStream(content, content.available()));

            // 生成预签名 URL（有效期 7 天）
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(java.time.Duration.ofDays(7))
                    .getObjectRequest(builder -> builder.bucket(bucket).key(fileName).build()).build();
            String presignedObjectUrl = presigner.presignGetObject(presignRequest).url().toString();

            return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).errmsg(ErrorCode.SUCCESS.getDesc())
                    .data(Material.builder().name(fileName).path(this.context.getPrefix() + fileName)
                            .url(presignedObjectUrl).build())
                    .build();
        } catch (SdkException | IOException e) {
            Logger.error("file upload failed: {}", e.getMessage());
            return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
        }
    }

    @Override
    public Message upload(String bucket, String fileName, byte[] content) {
        try {
            PutObjectRequest request = PutObjectRequest.builder().bucket(bucket).key(fileName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM).build();
            client.putObject(request, RequestBody.fromBytes(content));

            // 生成预签名 URL（有效期 7 天）
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(java.time.Duration.ofDays(7))
                    .getObjectRequest(builder -> builder.bucket(bucket).key(fileName).build()).build();
            String presignedObjectUrl = presigner.presignGetObject(presignRequest).url().toString();

            return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).errmsg(ErrorCode.SUCCESS.getDesc())
                    .data(Material.builder().name(fileName).path(this.context.getPrefix() + fileName)
                            .url(presignedObjectUrl).build())
                    .build();
        } catch (SdkException e) {
            Logger.error("file upload failed: {}", e.getMessage());
            return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
        }
    }

    @Override
    public Message remove(String fileName) {
        return remove(this.context.getBucket(), fileName);
    }

    @Override
    public Message remove(String bucket, String fileName) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(bucket).key(fileName).build();
            client.deleteObject(request);
            return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).errmsg(ErrorCode.SUCCESS.getDesc()).build();
        } catch (SdkException e) {
            Logger.error("file remove failed: {}", e.getMessage());
            return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
        }
    }

    @Override
    public Message remove(String bucket, Path path) {
        return remove(bucket, path.toString());
    }

}