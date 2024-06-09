/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Builder;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.Message;
import org.miaixz.bus.storage.magic.Property;

import java.io.*;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 存储服务-MinIO
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MinioOssProvider extends AbstractProvider {

    private MinioClient client;

    public MinioOssProvider(Context context) {
        this.context = context;
        Assert.notBlank(this.context.getPrefix(), "[prefix] not defined");
        Assert.notBlank(this.context.getEndpoint(), "[endpoint] not defined");
        Assert.notBlank(this.context.getBucket(), "[bucket] not defined");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] not defined");
        Assert.notBlank(this.context.getSecretKey(), "[secretKey] not defined");
        Assert.notNull(this.context.isSecure(), "[secure] not defined");
        Assert.notBlank(StringKit.toString(this.context.getReadTimeout()), "[readTimeout] not defined");
        Assert.notBlank(StringKit.toString(this.context.getConnectTimeout()), "[connectTimeout] not defined");
        Assert.notBlank(StringKit.toString(this.context.getWriteTimeout()), "[writeTimeout] not defined");

        this.client = MinioClient.builder()
                .endpoint(this.context.getEndpoint())
                .credentials(this.context.getAccessKey(), this.context.getSecretKey())
                .build();

        this.client.setTimeout(
                Duration.ofSeconds(this.context.getConnectTimeout() != 0 ? this.context.getConnectTimeout() : 10).toMillis(),
                Duration.ofSeconds(this.context.getWriteTimeout() != 60 ? this.context.getWriteTimeout() : 60).toMillis(),
                Duration.ofSeconds(this.context.getReadTimeout() != 0 ? this.context.getReadTimeout() : 10).toMillis()
        );
    }

    @Override
    public Message download(String fileName) {
        return download(this.context.getBucket(), fileName);
    }

    @Override
    public Message download(String bucket, String fileName) {
        try {
            InputStream inputStream = this.client.getObject(GetObjectArgs.builder().bucket(bucket).object(fileName).build());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            return Message.builder()
                    .errcode(Builder.ErrorCode.SUCCESS.getCode())
                    .errmsg(Builder.ErrorCode.SUCCESS.getMsg())
                    .data(bufferedReader)
                    .build();
        } catch (Exception e) {
            Logger.error("file download failed", e.getMessage());
        }
        return Message.builder()
                .errcode(Builder.ErrorCode.FAILURE.getCode())
                .errmsg(Builder.ErrorCode.FAILURE.getMsg())
                .build();
    }

    @Override
    public Message download(String bucket, String fileName, File file) {
        try {
            InputStream inputStream = this.client.getObject(GetObjectArgs.builder().bucket(bucket).object(fileName).build());
            OutputStream outputStream = new FileOutputStream(file);
            IoKit.copy(inputStream, outputStream);
            return Message.builder()
                    .errcode(Builder.ErrorCode.SUCCESS.getCode())
                    .errmsg(Builder.ErrorCode.SUCCESS.getMsg())
                    .build();
        } catch (Exception e) {
            Logger.error("file download failed", e.getMessage());
        }
        return Message.builder()
                .errcode(Builder.ErrorCode.FAILURE.getCode())
                .errmsg(Builder.ErrorCode.FAILURE.getMsg())
                .build();
    }

    @Override
    public Message download(String fileName, File file) {
        return download(this.context.getBucket(), fileName, file);
    }

    @Override
    public Message list() {
        Iterable<Result<Item>> iterable = this.client.listObjects(ListObjectsArgs.builder().bucket(this.context.getBucket()).build());
        return Message.builder()
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .errmsg(Builder.ErrorCode.SUCCESS.getMsg())
                .data(StreamSupport
                        .stream(iterable.spliterator(), true)
                        .map(itemResult -> {
                            try {
                                Property storageItem = new Property();
                                Item item = itemResult.get();
                                storageItem.setName(item.objectName());
                                storageItem.setSize(StringKit.toString(item.size()));
                                Map<String, Object> extend = new HashMap<>();
                                extend.put("tag", item.etag());
                                extend.put("storageClass", item.storageClass());
                                extend.put("lastModified", item.lastModified());
                                storageItem.setExtend(extend);
                                return storageItem;
                            } catch (NoSuchAlgorithmException |
                                     InsufficientDataException |
                                     IOException |
                                     InvalidKeyException |
                                     ErrorResponseException |
                                     InternalException e) {
                                return Message.builder()
                                        .errcode(Builder.ErrorCode.FAILURE.getCode())
                                        .errmsg(Builder.ErrorCode.FAILURE.getMsg())
                                        .build();
                            } catch (ServerException e) {
                                throw new RuntimeException(e);
                            } catch (InvalidResponseException e) {
                                throw new RuntimeException(e);
                            } catch (XmlParserException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public Message rename(String oldName, String newName) {
        return Message.builder()
                .errcode(Builder.ErrorCode.FAILURE.getCode())
                .errmsg(Builder.ErrorCode.FAILURE.getMsg())
                .build();
    }

    @Override
    public Message rename(String bucket, String oldName, String newName) {
        return Message.builder()
                .errcode(Builder.ErrorCode.FAILURE.getCode())
                .errmsg(Builder.ErrorCode.FAILURE.getMsg())
                .build();
    }

    @Override
    public Message upload(String bucket, byte[] content) {
        InputStream stream = new ByteArrayInputStream(content);
        return upload(this.context.getBucket(), bucket, stream);
    }

    @Override
    public Message upload(String bucket, String fileName, InputStream content) {
        try {
            this.client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(fileName)
                    .stream(content, content.available(), -1)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .build());
            return Message.builder()
                    .errcode(Builder.ErrorCode.SUCCESS.getCode())
                    .errmsg(Builder.ErrorCode.SUCCESS.getMsg())
                    .data(Property.builder()
                            .name(fileName)
                            .path(this.context.getPrefix() + fileName))
                    .build();
        } catch (Exception e) {
            Logger.error("file upload failed", e.getMessage());
        }
        return Message.builder()
                .errcode(Builder.ErrorCode.FAILURE.getCode())
                .errmsg(Builder.ErrorCode.FAILURE.getMsg())
                .build();
    }

    @Override
    public Message upload(String bucket, String fileName, byte[] content) {
        return upload(bucket, fileName, new ByteArrayInputStream(content));
    }

    @Override
    public Message remove(String fileName) {
        return remove(this.context.getBucket(), fileName);
    }

    @Override
    public Message remove(String bucket, String fileName) {
        try {
            this.client.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(fileName)
                    .build());
            return Message.builder()
                    .errcode(Builder.ErrorCode.SUCCESS.getCode())
                    .errmsg(Builder.ErrorCode.SUCCESS.getMsg())
                    .build();
        } catch (Exception e) {
            Logger.error("file remove failed ", e.getMessage());
        }
        return Message.builder()
                .errcode(Builder.ErrorCode.FAILURE.getCode())
                .errmsg(Builder.ErrorCode.FAILURE.getMsg())
                .build();
    }

    @Override
    public Message remove(String bucket, Path path) {
        return remove(bucket, path.toString());
    }

}
