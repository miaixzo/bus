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
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Builder;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.ErrorCode;
import org.miaixz.bus.storage.magic.Material;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/**
 * 存储服务-阿里云
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AliYunOssProvider extends AbstractProvider {

    private final S3Client client;
    private final S3Presigner presigner;

    /**
     * 使用给定的上下文构造阿里云 OSS 提供者。初始化 S3 客户端和预签名器，使用提供的凭证和端点配置
     *
     * @param context 存储上下文，包含端点、存储桶、访问密钥、秘密密钥等配置
     * @throws IllegalArgumentException 如果缺少或无效的必需上下文参数
     */
    public AliYunOssProvider(Context context) {
        this.context = context;

        Assert.notBlank(this.context.getEndpoint(), "[endpoint] cannot be blank");
        Assert.notBlank(this.context.getBucket(), "[bucket] cannot be blank");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] cannot be blank");
        Assert.notBlank(this.context.getSecretKey(), "[secretKey] cannot be blank");

        long readTimeout = this.context.getReadTimeout() != 0 ? this.context.getReadTimeout() : 10;
        long writeTimeout = this.context.getWriteTimeout() != 0 ? this.context.getWriteTimeout() : 60;

        ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                .apiCallTimeout(Duration.ofSeconds(writeTimeout)).apiCallAttemptTimeout(Duration.ofSeconds(readTimeout))
                .build();

        AwsBasicCredentials credentials = AwsBasicCredentials.create(this.context.getAccessKey(),
                this.context.getSecretKey());

        this.client = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(this.context.getEndpoint()))
                .region(Region.of(StringKit.isBlank(this.context.getRegion()) ? "us-east-1" : this.context.getRegion()))
                .overrideConfiguration(overrideConfig)
                .serviceConfiguration(s -> s.pathStyleAccessEnabled(this.context.isPathStyle())).build();

        this.presigner = S3Presigner.builder().credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(this.context.getEndpoint()))
                .region(Region.of(StringKit.isBlank(this.context.getRegion()) ? "us-east-1" : this.context.getRegion()))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(this.context.isPathStyle())
                        .chunkedEncodingEnabled(false).build())
                .build();
    }

    /**
     * 从默认存储桶下载文件。
     *
     * @param fileName 文件名
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message download(String fileName) {
        return download(this.context.getBucket(), fileName);
    }

    /**
     * 从指定存储桶下载文件。
     *
     * @param bucket   存储桶
     * @param fileName 文件名
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message download(String bucket, String fileName) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, Normal.EMPTY, fileName);
            GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(objectKey).build();
            InputStream inputStream = client.getObject(request);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(bufferedReader).build();
        } catch (Exception e) {
            Logger.error("Failed to download file: {} from bucket: {}. Error: {}", fileName, bucket, e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * 从默认存储桶下载文件并保存到本地文件。
     *
     * @param fileName 文件名
     * @param file     文件
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message download(String fileName, File file) {
        return download(this.context.getBucket(), fileName, file);
    }

    /**
     * 从指定存储桶下载文件并保存到本地文件。
     *
     * @param bucket   存储桶
     * @param fileName 文件名
     * @param file     文件
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message download(String bucket, String fileName, File file) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, Normal.EMPTY, fileName);
            GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(objectKey).build();
            InputStream inputStream = client.getObject(request);
            try (OutputStream outputStream = new FileOutputStream(file)) {
                IoKit.copy(inputStream, outputStream);
            }
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (Exception e) {
            Logger.error("Failed to download file: {} from bucket: {} to local file: {}. Error: {}", fileName, bucket,
                    file.getAbsolutePath(), e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * 列出默认存储桶中的文件。
     *
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message list() {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(this.context.getBucket())
                    .prefix(StringKit.isBlank(context.getPrefix()) ? null
                            : Builder.buildNormalizedPrefix(context.getPrefix()) + "/")
                    .build();
            ListObjectsV2Response response = client.listObjectsV2(request);
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(response.contents().stream().map(item -> {
                        Map<String, Object> extend = new HashMap<>();
                        extend.put("tag", item.eTag());
                        extend.put("storageClass", item.storageClassAsString());
                        extend.put("lastModified", item.lastModified());
                        return Material.builder().name(item.key()).size(StringKit.toString(item.size())).extend(extend)
                                .build();
                    }).collect(Collectors.toList())).build();
        } catch (Exception e) {
            Logger.error("Failed to list objects in bucket: {}. Error: {}", this.context.getBucket(), e.getMessage(),
                    e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * 重命名文件。
     *
     * @param oldName 原文件名
     * @param newName 新文件名
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message rename(String oldName, String newName) {
        return rename(this.context.getBucket(), Normal.EMPTY, oldName, newName);
    }

    /**
     * 在默认存储桶中重命名文件。
     *
     * @param path    路径
     * @param oldName 原文件名
     * @param newName 新文件名
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message rename(String path, String oldName, String newName) {
        return rename(this.context.getBucket(), path, oldName, newName);
    }

    /**
     * 在指定存储桶和路径中重命名文件。
     *
     * @param bucket  存储桶
     * @param path    路径
     * @param oldName 原文件名
     * @param newName 新文件名
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message rename(String bucket, String path, String oldName, String newName) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String oldObjectKey = Builder.buildObjectKey(prefix, path, oldName);
            String newObjectKey = Builder.buildObjectKey(prefix, path, newName);
            boolean keyExists = true;
            try {
                HeadObjectRequest headRequest = HeadObjectRequest.builder().bucket(bucket).key(oldObjectKey).build();
                client.headObject(headRequest);
            } catch (Exception e) {
                keyExists = false;
            }
            if (keyExists) {
                CopyObjectRequest copyRequest = CopyObjectRequest.builder().sourceBucket(bucket).sourceKey(oldObjectKey)
                        .destinationBucket(bucket).destinationKey(newObjectKey).build();
                client.copyObject(copyRequest);
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder().bucket(bucket).key(oldObjectKey)
                        .build();
                client.deleteObject(deleteRequest);
            }
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (Exception e) {
            Logger.error("Failed to rename file from: {} to: {} in bucket: {} with path: {}, error: {}", oldName,
                    newName, bucket, path, e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * 上传字节数组到默认存储桶。
     *
     * @param fileName 文件名
     * @param content  字节数组
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message upload(String fileName, byte[] content) {
        return upload(this.context.getBucket(), Normal.EMPTY, fileName, content);
    }

    /**
     * 上传字节数组到默认存储桶指定路径。
     *
     * @param path     路径
     * @param fileName 文件名
     * @param content  字节数组
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message upload(String path, String fileName, byte[] content) {
        return upload(this.context.getBucket(), path, fileName, content);
    }

    /**
     * 上传字节数组到指定存储桶和路径。
     *
     * @param bucket   存储桶
     * @param path     路径
     * @param fileName 文件名
     * @param content  字节数组
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message upload(String bucket, String path, String fileName, byte[] content) {
        return upload(bucket, path, fileName, new ByteArrayInputStream(content));
    }

    /**
     * 上传输入流到默认存储桶。
     *
     * @param fileName 文件名
     * @param content  输入流
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message upload(String fileName, InputStream content) {
        return upload(this.context.getBucket(), Normal.EMPTY, fileName, content);
    }

    /**
     * 上传输入流到默认存储桶指定路径。
     *
     * @param path     路径
     * @param fileName 文件名
     * @param content  输入流
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message upload(String path, String fileName, InputStream content) {
        return upload(this.context.getBucket(), path, fileName, content);
    }

    /**
     * 上传输入流到指定存储桶和路径。
     *
     * @param bucket   存储桶
     * @param path     路径
     * @param fileName 文件名
     * @param content  输入流
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message upload(String bucket, String path, String fileName, InputStream content) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, path, fileName);
            PutObjectRequest request = PutObjectRequest.builder().bucket(bucket).key(objectKey)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM).build();
            client.putObject(request, RequestBody.fromInputStream(content, content.available()));

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofDays(7)).getObjectRequest(r -> r.bucket(bucket).key(objectKey))
                    .build();
            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(Material.builder().name(fileName).url(presignedUrl).path(objectKey).build()).build();
        } catch (Exception e) {
            Logger.error("Failed to upload file: {} to bucket: {} with path: {}, error: {}", fileName, bucket, path,
                    e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * 从默认存储桶删除文件。
     *
     * @param fileName 文件名
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message remove(String fileName) {
        return remove(this.context.getBucket(), Normal.EMPTY, fileName);
    }

    /**
     * 从指定存储桶删除文件。
     *
     * @param bucket   存储桶
     * @param fileName 文件名
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message remove(String bucket, String fileName) {
        return remove(bucket, Normal.EMPTY, fileName);
    }

    /**
     * 从指定存储桶和路径删除文件。
     *
     * @param bucket   存储桶
     * @param path     路径
     * @param fileName 文件名
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message remove(String bucket, String path, String fileName) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, path, fileName);
            DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(bucket).key(objectKey).build();
            client.deleteObject(request);
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (Exception e) {
            Logger.error("Failed to remove file: {} from bucket: {} with path: {}, error: {}", fileName, bucket, path,
                    e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * 从指定存储桶删除文件（基于路径）。
     *
     * @param bucket 存储桶
     * @param path   目标路径
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message remove(String bucket, Path path) {
        return remove(bucket, path.toString(), Normal.EMPTY);
    }

}