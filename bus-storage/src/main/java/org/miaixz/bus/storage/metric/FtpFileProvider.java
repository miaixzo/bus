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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.ftp.CommonsFtp;
import org.miaixz.bus.extra.ftp.Ftp;
import org.miaixz.bus.extra.ssh.Connector;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Builder;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.ErrorCode;
import org.miaixz.bus.storage.magic.Material;

/**
 * 存储服务-FTP
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FtpFileProvider extends AbstractProvider {

    private final Ftp client;

    /**
     * 构造函数，初始化 FTP 客户端。
     *
     * @param context 存储上下文，包含端点、存储桶、访问密钥、秘密密钥等配置
     * @throws IllegalArgumentException 如果配置参数无效或初始化失败
     */
    public FtpFileProvider(Context context) {
        this.context = context;

        Assert.notBlank(this.context.getEndpoint(), "[endpoint] cannot be blank");
        Assert.notBlank(this.context.getBucket(), "[bucket] cannot be blank");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] cannot be blank");
        Assert.notBlank(this.context.getSecretKey(), "[secretKey] cannot be blank");

        // 从 endpoint 解析主机和端口
        String host = parseHostFromEndpoint(context.getEndpoint());
        int port = parsePortFromEndpoint(context.getEndpoint());
        String username = context.getAccessKey();
        String password = context.getSecretKey();

        try {
            Connector connector = new Connector();
            connector.setHost(host);
            connector.setPort(port != 0 ? port : 21); // 默认 FTP 端口 21
            connector.setUser(username);
            connector.setPassword(password);
            this.client = CommonsFtp.of(connector, Charset.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to initialize FTP client: " + e.getMessage(), e);
        }
    }

    /**
     * 从默认存储桶下载文件。
     *
     * @param fileName 文件名
     * @return 处理结果，包含文件内容流或错误信息
     */
    @Override
    public Message download(String fileName) {
        return download(Normal.EMPTY, fileName);
    }

    /**
     * 从指定存储桶下载文件。
     *
     * @param bucket   存储桶名称
     * @param fileName 文件名
     * @return 处理结果，包含文件内容流或错误信息
     */
    @Override
    public Message download(String bucket, String fileName) {
        try {
            String objectKey = getAbsolutePath(bucket, Normal.EMPTY, fileName);
            InputStream inputStream = client.getFileStream(objectKey);
            if (inputStream == null) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("File not found").build();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(reader).build();
        } catch (InternalException e) {
            Logger.error("Failed to download file: {} from bucket: {}. Error: {}", fileName, bucket, e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * 从默认存储桶下载文件并保存到本地文件。
     *
     * @param fileName 文件名
     * @param file     本地目标文件
     * @return 处理结果，包含成功或错误信息
     */
    @Override
    public Message download(String fileName, File file) {
        return download(Normal.EMPTY, fileName, file);
    }

    /**
     * 从指定存储桶下载文件并保存到本地文件。
     *
     * @param bucket   存储桶名称
     * @param fileName 文件名
     * @param file     本地目标文件
     * @return 处理结果，包含成功或错误信息
     */
    @Override
    public Message download(String bucket, String fileName, File file) {
        try {
            String objectKey = getAbsolutePath(bucket, Normal.EMPTY, fileName);
            client.download(objectKey, file);
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (InternalException e) {
            Logger.error("Failed to download file: {} from bucket: {} to local file: {}. Error: {}", fileName, bucket,
                    file.getAbsolutePath(), e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * 列出默认存储桶中的文件。
     *
     * @return 处理结果，包含文件列表或错误信息
     */
    @Override
    public Message list() {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            List<String> files = client.ls(prefix);
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(files.stream().map(fileName -> {
                        Map<String, Object> extend = new HashMap<>();
                        return Material.builder().name(fileName).extend(extend).build();
                    }).collect(Collectors.toList())).build();
        } catch (InternalException e) {
            Logger.error("Failed to list files in path: {}. Error: {}", context.getPrefix(), e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * 重命名默认存储桶中的文件。
     *
     * @param oldName 原文件名
     * @param newName 新文件名
     * @return 处理结果，包含成功或错误信息
     */
    @Override
    public Message rename(String oldName, String newName) {
        return rename(Normal.EMPTY, oldName, newName);
    }

    /**
     * 在默认存储桶的指定路径中重命名文件。
     *
     * @param path    路径
     * @param oldName 原文件名
     * @param newName 新文件名
     * @return 处理结果，包含成功或错误信息
     */
    @Override
    public Message rename(String path, String oldName, String newName) {
        return rename(Normal.EMPTY, path, oldName, newName);
    }

    /**
     * 在指定存储桶和路径中重命名文件。
     *
     * @param bucket  存储桶名称
     * @param path    路径
     * @param oldName 原文件名
     * @param newName 新文件名
     * @return 处理结果，包含成功或错误信息
     */
    @Override
    public Message rename(String bucket, String path, String oldName, String newName) {
        try {
            String oldObjectKey = getAbsolutePath(bucket, path, oldName);
            String newObjectKey = getAbsolutePath(bucket, path, newName);
            if (!client.exist(oldObjectKey)) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("File not found").build();
            }
            client.rename(oldObjectKey, newObjectKey);
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (InternalException e) {
            Logger.error("Failed to rename file from {} to {} in bucket: {} path: {}. Error: {}", oldName, newName,
                    bucket, path, e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * 上传字节数组内容到默认存储桶。
     *
     * @param fileName 文件名
     * @param content  字节数组内容
     * @return 处理结果，包含上传的文件信息或错误信息
     */
    @Override
    public Message upload(String fileName, byte[] content) {
        return upload(Normal.EMPTY, fileName, content);
    }

    /**
     * 上传字节数组内容到默认存储桶的指定路径。
     *
     * @param path     路径
     * @param fileName 文件名
     * @param content  字节数组内容
     * @return 处理结果，包含上传的文件信息或错误信息
     */
    @Override
    public Message upload(String path, String fileName, byte[] content) {
        return upload(Normal.EMPTY, path, fileName, content);
    }

    /**
     * 上传字节数组内容到指定存储桶和路径。
     *
     * @param bucket   存储桶名称
     * @param path     路径
     * @param fileName 文件名
     * @param content  字节数组内容
     * @return 处理结果，包含上传的文件信息或错误信息
     */
    @Override
    public Message upload(String bucket, String path, String fileName, byte[] content) {
        return upload(bucket, path, fileName, new ByteArrayInputStream(content));
    }

    /**
     * 上传输入流内容到默认存储桶。
     *
     * @param fileName 文件名
     * @param content  输入流内容
     * @return 处理结果，包含上传的文件信息或错误信息
     */
    @Override
    public Message upload(String fileName, InputStream content) {
        return upload(Normal.EMPTY, fileName, content);
    }

    /**
     * 上传输入流内容到默认存储桶的指定路径。
     *
     * @param path     路径
     * @param fileName 文件名
     * @param content  输入流内容
     * @return 处理结果，包含上传的文件信息或错误信息
     */
    @Override
    public Message upload(String path, String fileName, InputStream content) {
        return upload(Normal.EMPTY, path, fileName, content);
    }

    /**
     * 上传输入流内容到指定存储桶和路径。
     *
     * @param bucket   存储桶名称
     * @param path     路径
     * @param fileName 文件名
     * @param content  输入流内容
     * @return 处理结果，包含上传的文件信息或错误信息
     */
    @Override
    public Message upload(String bucket, String path, String fileName, InputStream content) {
        try {
            String objectKey = getAbsolutePath(bucket, path, fileName);
            String dirPath = objectKey.substring(0, objectKey.lastIndexOf(Symbol.SLASH));
            if (!client.isDir(dirPath)) {
                client.mkDirs(dirPath);
            }
            // 使用临时文件上传流内容
            File tempFile = File.createTempFile("ftp_upload_", ".tmp");
            try (OutputStream out = new FileOutputStream(tempFile)) {
                IoKit.copy(content, out);
            }
            client.uploadFile(dirPath, tempFile);
            tempFile.delete();
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(Material.builder().name(fileName).path(objectKey).build()).build();
        } catch (InternalException | IOException e) {
            Logger.error("Failed to upload file: {} to bucket: {} path: {}. Error: {}", fileName, bucket, path,
                    e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * 从默认存储桶删除文件。
     *
     * @param fileName 文件名
     * @return 处理结果，包含成功或错误信息
     */
    @Override
    public Message remove(String fileName) {
        return remove(Normal.EMPTY, fileName);
    }

    /**
     * 从默认存储桶的指定路径删除文件。
     *
     * @param path     路径
     * @param fileName 文件名
     * @return 处理结果，包含成功或错误信息
     */
    @Override
    public Message remove(String path, String fileName) {
        return remove(Normal.EMPTY, path, fileName);
    }

    /**
     * 从指定存储桶和路径删除文件。
     *
     * @param bucket   存储桶名称
     * @param path     路径
     * @param fileName 文件名
     * @return 处理结果，包含成功或错误信息
     */
    @Override
    public Message remove(String bucket, String path, String fileName) {
        try {
            String objectKey = getAbsolutePath(bucket, path, fileName);
            if (client.exist(objectKey)) {
                client.delFile(objectKey);
            }
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (InternalException e) {
            Logger.error("Failed to remove file: {} from bucket: {} path: {}. Error: {}", fileName, bucket, path,
                    e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * 从指定存储桶删除指定路径的文件。
     *
     * @param bucket 存储桶名称
     * @param path   文件路径
     * @return 处理结果，包含成功或错误信息
     */
    @Override
    public Message remove(String bucket, Path path) {
        return remove(bucket, path.toString(), Normal.EMPTY);
    }

    /**
     * 从 endpoint 解析主机，确保不包含端口信息。
     *
     * @param endpoint FTP 服务器地址，格式如 ftp://hostname:port 或 hostname
     * @return 主机名
     */
    private String parseHostFromEndpoint(String endpoint) {
        if (StringKit.isBlank(endpoint)) {
            return "";
        }
        // 移除协议头（如 ftp://）
        String host = endpoint.replaceFirst("^(ftp)://", "");
        // 移除端口和路径
        int colonIndex = host.indexOf(':');
        int slashIndex = host.indexOf('/');
        if (colonIndex != -1) {
            host = host.substring(0, colonIndex);
        } else if (slashIndex != -1) {
            host = host.substring(0, slashIndex);
        }
        return host;
    }

    /**
     * 从 endpoint 解析端口。
     *
     * @param endpoint FTP 服务器地址，格式如 ftp://hostname:port 或 hostname
     * @return 端口号，0 表示使用默认端口 21
     */
    private int parsePortFromEndpoint(String endpoint) {
        if (StringKit.isBlank(endpoint)) {
            return 0;
        }
        try {
            // 提取端口部分
            String portStr = endpoint.replaceFirst("^(ftp)://[^:]+:?", "");
            int slashIndex = portStr.indexOf('/');
            if (slashIndex != -1) {
                portStr = portStr.substring(0, slashIndex); // 移除路径部分
            }
            if (StringKit.isNotBlank(portStr)) {
                return Integer.parseInt(portStr);
            }
        } catch (NumberFormatException e) {
            Logger.warn("Invalid port in endpoint: {}. Using default port 21.", endpoint);
        }
        return 0; // 返回 0 表示使用默认端口 21
    }

    /**
     * 构建文件的绝对路径。
     *
     * @param bucket   存储桶名称，可为空
     * @param path     路径，可为空
     * @param fileName 文件名
     * @return 规范化后的绝对路径
     */
    private String getAbsolutePath(String bucket, String path, String fileName) {
        String prefix = StringKit.isBlank(bucket) ? Builder.buildNormalizedPrefix(context.getPrefix())
                : Builder.buildNormalizedPrefix(context.getPrefix() + bucket);
        return Builder.buildObjectKey(prefix, path, fileName);
    }

}