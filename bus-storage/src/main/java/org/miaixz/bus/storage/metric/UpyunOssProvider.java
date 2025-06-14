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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.center.date.Formatter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Httpd;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.bodys.RequestBody;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Builder;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.ErrorCode;
import org.miaixz.bus.storage.magic.Material;

/**
 * 存储服务-又拍云
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UpyunOssProvider extends AbstractProvider {

    private final Httpd httpd;

    /**
     * 使用给定的上下文构造又拍云提供者。初始化 OkHttp 客户端，使用提供的凭证和配置
     *
     * @param context 存储上下文，包含端点、存储桶、访问密钥、秘密密钥、区域等配置
     * @throws IllegalArgumentException 如果缺少或无效的必需上下文参数
     */
    public UpyunOssProvider(Context context) {
        this.context = context;

        Assert.notBlank(this.context.getEndpoint(), "[endpoint] cannot be blank");
        Assert.notBlank(this.context.getBucket(), "[bucket] cannot be blank");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] cannot be blank");
        Assert.notBlank(this.context.getSecretKey(), "[secretKey] cannot be blank");

        this.httpd = new Httpd();
    }

    /**
     * 生成又拍云 REST API 签名
     *
     * @param method        HTTP 方法
     * @param path          请求路径（如 /<bucket>/<path>）
     * @param date          GMT 时间
     * @param contentLength 请求体长度
     * @return 签名字符串
     */
    private String generateSignature(String method, String path, String date, long contentLength) {
        String signStr = String.format("%s&%s&%s&%d&%s", method, path, date, contentLength,
                org.miaixz.bus.crypto.Builder.md5(this.context.getSecretKey()));
        return org.miaixz.bus.crypto.Builder.md5(signStr);
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
            String path = "/" + bucket + "/" + objectKey;
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String signature = generateSignature("GET", path, date, 0);

            Request request = new Request.Builder().url(this.context.getEndpoint() + path)
                    .addHeader("Authorization", "UPYUN " + context.getAccessKey() + ":" + signature)
                    .addHeader("Date", date).get().build();

            try (Response response = httpd.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                InputStream inputStream = response.body().byteStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .data(bufferedReader).build();
            }
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
            String path = "/" + bucket + "/" + objectKey;
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String signature = generateSignature("GET", path, date, 0);

            Request request = new Request.Builder().url(this.context.getEndpoint() + path)
                    .addHeader("Authorization", "UPYUN " + context.getAccessKey() + ":" + signature)
                    .addHeader("Date", date).get().build();

            try (Response response = httpd.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                try (InputStream inputStream = response.body().byteStream();
                        OutputStream outputStream = new FileOutputStream(file)) {
                    IoKit.copy(inputStream, outputStream);
                }
                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .build();
            }
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
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String path = "/" + context.getBucket() + "/" + prefix;
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String signature = generateSignature("GET", path, date, 0);

            Request request = new Request.Builder().url(this.context.getEndpoint() + path)
                    .addHeader("Authorization", "UPYUN " + context.getAccessKey() + ":" + signature)
                    .addHeader("Date", date).addHeader("x-upyun-list-limit", "100").get().build();

            try (Response response = httpd.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                String responseBody = response.body().string();
                List<Material> files = new ArrayList<>();
                String[] lines = responseBody.split("\n");
                for (String line : lines) {
                    String[] parts = line.split("\t");
                    if (parts.length == 4) {
                        Map<String, Object> extend = new HashMap<>();
                        extend.put("tag", parts[0]);
                        extend.put("type", parts[1]);
                        extend.put("size", parts[2]);
                        extend.put("lastModified", parts[3]);
                        files.add(Material.builder().name(parts[0]).size(parts[2]).extend(extend).build());
                    }
                }
                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .data(files).build();
            }
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
            String oldPath = "/" + bucket + "/" + oldObjectKey;
            String newPath = "/" + bucket + "/" + newObjectKey;

            // 下载原文件内容
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String getSignature = generateSignature("GET", oldPath, date, 0);
            Request getRequest = new Request.Builder().url(this.context.getEndpoint() + oldPath)
                    .addHeader("Authorization", "UPYUN " + context.getAccessKey() + ":" + getSignature)
                    .addHeader("Date", date).get().build();

            byte[] content;
            try (Response response = httpd.newCall(getRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                content = response.body().bytes();
            }

            // 上传到新路径
            String putDate = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String putSignature = generateSignature("PUT", newPath, putDate, content.length);
            Request putRequest = new Request.Builder().url(this.context.getEndpoint() + newPath)
                    .addHeader("Authorization", "UPYUN " + context.getAccessKey() + ":" + putSignature)
                    .addHeader("Date", putDate).addHeader("Content-Length", String.valueOf(content.length))
                    .addHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM)
                    .put(RequestBody.create(MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM), content)).build();

            try (Response response = httpd.newCall(putRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
            }

            // 删除原文件
            String deleteDate = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String deleteSignature = generateSignature("DELETE", oldPath, deleteDate, 0);
            Request deleteRequest = new Request.Builder().url(this.context.getEndpoint() + oldPath)
                    .addHeader("Authorization", "UPYUN " + context.getAccessKey() + ":" + deleteSignature)
                    .addHeader("Date", deleteDate).delete().build();

            try (Response response = httpd.newCall(deleteRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
            }

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (Exception e) {
            Logger.error("Failed to rename file from: {} to: {} in bucket: {} with path: {}, error: {}", oldName,
                    newName, bucket, path, e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * 上传字节数组内容到默认存储桶。
     *
     * @param fileName 文件名名
     * @param content  字节数组
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message upload(String fileName, byte[] content) {
        return upload(this.context.getBucket(), Normal.EMPTY, fileName, content);
    }

    /**
     * 上传字节数组内容到指定存储桶。
     *
     * @param bucket   存储桶
     * @param fileName 文件名名
     * @param content  字节数组
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message upload(String bucket, String fileName, byte[] content) {
        return upload(bucket, Normal.EMPTY, fileName, content);
    }

    /**
     * 上传字节数组内容到指定存储桶和路径。
     *
     * @param bucket   存储桶
     * @param path     路径
     * @param fileName 文件名名
     * @param content  字节数组
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message upload(String bucket, String path, String fileName, byte[] content) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, path, fileName);
            String requestPath = "/" + bucket + "/" + objectKey;
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String signature = generateSignature("PUT", requestPath, date, content.length);

            Request request = new Request.Builder().url(this.context.getEndpoint() + requestPath)
                    .addHeader("Authorization", "UPYUN " + context.getAccessKey() + ":" + signature)
                    .addHeader("Date", date).addHeader("Content-Length", String.valueOf(content.length))
                    .addHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM)
                    .put(RequestBody.create(MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM), content)).build();

            try (Response response = httpd.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .data(Material.builder().name(fileName).path(objectKey).build()).build();
            }
        } catch (Exception e) {
            Logger.error("Failed to upload file: {} to bucket: {} with path: {}, error: {}", fileName, bucket, path,
                    e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * 上传输入流内容到默认存储桶。
     *
     * @param fileName 文件名名
     * @param content  输入流
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message upload(String fileName, InputStream content) {
        return upload(this.context.getBucket(), Normal.EMPTY, fileName, content);
    }

    /**
     * 上传输入流内容到默认存储桶指定路径。
     *
     * @param path     路径
     * @param fileName 文件名名
     * @param content  输入流
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message upload(String path, String fileName, InputStream content) {
        return upload(this.context.getBucket(), path, fileName, content);
    }

    /**
     * 上传输入流内容到指定存储桶和路径。
     *
     * @param bucket   存储桶
     * @param path     路径
     * @param fileName 文件名名
     * @param content  输入流
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message upload(String bucket, String path, String fileName, InputStream content) {
        try {
            byte[] contentBytes = IoKit.readBytes(content);
            return upload(bucket, path, fileName, contentBytes);
        } catch (Exception e) {
            Logger.error("Failed to upload file: {} to bucket: {} with path: {}, error: {}", fileName, bucket, path,
                    e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * 从默认存储桶删除文件。
     *
     * @param fileName 要删除的文件名
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
     * @param fileName 要删除的文件名
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
     * @param fileName 要删除的文件名
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message remove(String bucket, String path, String fileName) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, path, fileName);
            String requestPath = "/" + bucket + "/" + objectKey;
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String signature = generateSignature("DELETE", requestPath, date, 0);

            Request request = new Request.Builder().url(this.context.getEndpoint() + requestPath)
                    .addHeader("Authorization", "UPYUN " + context.getAccessKey() + ":" + signature)
                    .addHeader("Date", date).delete().build();

            try (Response response = httpd.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .build();
            }
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
     * @param path   要删除的文件路径
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message remove(String bucket, Path path) {
        return remove(bucket, path.toString(), Normal.EMPTY);
    }

}