/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.storage.provider;

import com.google.common.collect.Maps;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.storage.Builder;
import org.aoju.bus.storage.Context;
import org.aoju.bus.storage.magic.Attachs;
import org.aoju.bus.storage.magic.Message;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 存储服务-MinIO
 *
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
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
        Assert.notBlank(StringKit.toString(this.context.getReadTimeout()), "[readTimeout] not defined");
        try {
            this.client = new MinioClient(
                    this.context.getEndpoint(),
                    this.context.getAccessKey(),
                    this.context.getSecretKey(),
                    this.context.isSecure()
            );
            this.client.setTimeout(
                    Duration.ofSeconds(this.context.getConnectTimeout() != 0 ? this.context.getConnectTimeout() : 10).toMillis(),
                    Duration.ofSeconds(this.context.getWriteTimeout() != 60 ? this.context.getWriteTimeout() : 60).toMillis(),
                    Duration.ofSeconds(this.context.getReadTimeout() != 0 ? this.context.getReadTimeout() : 10).toMillis()
            );
        } catch (InvalidPortException | InvalidEndpointException ex) {
            throw new InstrumentException(ex.getMessage());
        }
    }

    @Override
    public Message download(String fileName) {
        return download(this.context.getBucket(), fileName);
    }

    @Override
    public Message download(String bucket, String fileName) {
        try {
            InputStream inputStream = this.client.getObject(bucket, fileName);
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
            InputStream inputStream = this.client.getObject(bucket, fileName);
            OutputStream outputStream = new FileOutputStream(file);
            IoKit.copy(inputStream, outputStream);
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
        try {
            Iterable<Result<Item>> iterable = this.client.listObjects(this.context.getBucket());

            return Message.builder()
                    .errcode(Builder.ErrorCode.SUCCESS.getCode())
                    .errmsg(Builder.ErrorCode.SUCCESS.getMsg())
                    .data(StreamSupport
                            .stream(iterable.spliterator(), true)
                            .map(itemResult -> {
                                try {
                                    Attachs storageItem = new Attachs();
                                    Item item = itemResult.get();
                                    storageItem.setName(item.objectName());
                                    storageItem.setSize(StringKit.toString(item.objectSize()));
                                    Map<String, Object> extend = Maps.newHashMap();
                                    extend.put("tag", item.etag());
                                    extend.put("storageClass", item.storageClass());
                                    extend.put("lastModified", item.lastModified());
                                    storageItem.setExtend(extend);
                                    return storageItem;
                                } catch (InvalidBucketNameException |
                                        NoSuchAlgorithmException |
                                        InsufficientDataException |
                                        IOException |
                                        InvalidKeyException |
                                        NoResponseException |
                                        XmlPullParserException |
                                        ErrorResponseException |
                                        InternalException e) {
                                    return Message.builder()
                                            .errcode(Builder.ErrorCode.FAILURE.getCode())
                                            .errmsg(Builder.ErrorCode.FAILURE.getMsg())
                                            .build();
                                }
                            })
                            .collect(Collectors.toList()))
                    .build();
        } catch (XmlPullParserException e) {
            Logger.error("file list failed", e.getMessage());
        }
        return Message.builder()
                .errcode(Builder.ErrorCode.FAILURE.getCode())
                .errmsg(Builder.ErrorCode.FAILURE.getMsg())
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
            this.client.putObject(bucket, fileName, content, content.available(),
                    MediaType.APPLICATION_OCTET_STREAM);
            return Message.builder()
                    .errcode(Builder.ErrorCode.SUCCESS.getCode())
                    .errmsg(Builder.ErrorCode.SUCCESS.getMsg())
                    .data(Attachs.builder()
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
            this.client.removeObject(bucket, fileName);
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
