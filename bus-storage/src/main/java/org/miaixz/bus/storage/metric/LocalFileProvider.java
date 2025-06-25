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
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Builder;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.ErrorCode;
import org.miaixz.bus.storage.magic.Material;

/**
 * 存储服务-本地文件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LocalFileProvider extends AbstractProvider {

    /**
     * 构造本地文件存储提供者，初始化存储上下文
     *
     * @param context 存储上下文，包含存储路径（region）、存储桶等配置
     * @throws IllegalArgumentException 如果缺少或无效的必需上下文参数
     */
    public LocalFileProvider(Context context) {
        this.context = context;

        Assert.notBlank(this.context.getBucket(), "[bucket] cannot be blank");
        Assert.notBlank(this.context.getRegion(), "[region] cannot be blank");
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
            Path filePath = Paths.get(context.getRegion(), bucket, objectKey);
            if (!Files.exists(filePath)) {
                throw new IOException("File does not exist: " + filePath);
            }
            InputStream inputStream = Files.newInputStream(filePath);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(bufferedReader).build();
        } catch (IOException e) {
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
            Path sourcePath = Paths.get(context.getRegion(), bucket, objectKey);
            if (!Files.exists(sourcePath)) {
                throw new IOException("File does not exist: " + sourcePath);
            }
            Files.copy(sourcePath, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (IOException e) {
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
            Path bucketPath = Paths.get(context.getRegion(), this.context.getBucket());
            if (!Files.exists(bucketPath)) {
                throw new IOException("Bucket directory does not exist: " + bucketPath);
            }
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(Files.walk(bucketPath).filter(Files::isRegularFile).map(path -> {
                        String relativePath = bucketPath.relativize(path).toString();
                        Map<String, Object> extend = new HashMap<>();
                        try {
                            extend.put("lastModified", Files.getLastModifiedTime(path).toInstant());
                        } catch (IOException e) {
                            extend.put("lastModified", null);
                        }
                        return Material.builder().name(relativePath).size(StringKit.toString(path.toFile().length()))
                                .extend(extend).build();
                    }).collect(Collectors.toList())).build();
        } catch (IOException e) {
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
            Path oldPath = Paths.get(context.getRegion(), bucket, oldObjectKey);
            Path newPath = Paths.get(context.getRegion(), bucket, newObjectKey);
            if (!Files.exists(oldPath)) {
                throw new IOException("Source file does not exist: " + oldPath);
            }
            Files.createDirectories(newPath.getParent());
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (IOException e) {
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
        return upload(Normal.EMPTY, fileName, content);
    }

    /**
     * 上传字节数组内容到指定存储桶。
     *
     * @param path     路径
     * @param fileName 文件名名
     * @param content  字节数组
     * @return 处理结果 {@link Message}
     */
    @Override
    public Message upload(String path, String fileName, byte[] content) {
        return upload(this.context.getBucket(), path, fileName, content);
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
        return upload(bucket, path, fileName, new ByteArrayInputStream(content));
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
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, path, fileName);
            Path destPath = Paths.get(context.getRegion(), bucket, objectKey);
            Files.createDirectories(destPath.getParent());
            try (OutputStream out = Files.newOutputStream(destPath, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                IoKit.copy(content, out);
            }
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(Material.builder().name(fileName).path(objectKey).build()).build();
        } catch (IOException e) {
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
            Path filePath = Paths.get(context.getRegion(), bucket, objectKey);
            Files.deleteIfExists(filePath);
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (IOException e) {
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