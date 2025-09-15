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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Builder;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.ErrorCode;
import org.miaixz.bus.storage.magic.Material;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileRenameInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskEntry;
import com.hierynomus.smbj.share.DiskShare;

/**
 * 存储服务-SMB共享文件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SmbFileProvider extends AbstractProvider {

    private final SMBClient client;
    private Connection connection;
    private Session session;
    private DiskShare share;

    /**
     * 构造函数，初始化 SMB 客户端。
     *
     * @param context 存储上下文，包含端点、存储桶、访问密钥、秘密密钥等配置
     * @throws IllegalArgumentException 如果配置参数无效或初始化失败
     */
    public SmbFileProvider(Context context) {
        this.context = context;
        Assert.notBlank(this.context.getEndpoint(), "[endpoint] cannot be blank");
        Assert.notBlank(this.context.getBucket(), "[bucket] cannot be blank");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] cannot be blank");
        Assert.notBlank(this.context.getSecretKey(), "[secretKey] cannot be blank");

        // 初始化 SMB 客户端
        this.client = new SMBClient();

        // 从 endpoint 解析服务器地址和端口
        String server = parseServerFromEndpoint(context.getEndpoint());
        int port = parsePortFromEndpoint(context.getEndpoint());
        String shareName = context.getBucket();
        String username = context.getAccessKey();
        String password = context.getSecretKey();
        String domain = parseDomainFromEndpoint(context.getEndpoint());

        try {
            // 建立连接
            this.connection = client.connect(server, port);
            // 创建认证上下文
            AuthenticationContext ac = new AuthenticationContext(username, password.toCharArray(), domain);
            // 建立会话
            this.session = connection.authenticate(ac);
            // 连接到共享
            this.share = (DiskShare) session.connectShare(shareName);
        } catch (Exception e) {
            closeResources();
            throw new IllegalArgumentException("Failed to initialize SMB client: " + e.getMessage(), e);
        }
    }

    /**
     * 关闭资源
     */
    private void closeResources() {
        try {
            if (share != null) {
                share.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            Logger.error("Error while closing SMB resources: {}", e.getMessage(), e);
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
            if (!share.fileExists(objectKey)) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("File not found").build();
            }

            com.hierynomus.smbj.share.File smbFile = share.openFile(objectKey, EnumSet.of(AccessMask.GENERIC_READ),
                    null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);

            InputStream inputStream = smbFile.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(reader).build();
        } catch (Exception e) {
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
        try (OutputStream outputStream = new FileOutputStream(file)) {
            String objectKey = getAbsolutePath(bucket, Normal.EMPTY, fileName);
            if (!share.fileExists(objectKey)) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("File not found").build();
            }

            com.hierynomus.smbj.share.File smbFile = share.openFile(objectKey, EnumSet.of(AccessMask.GENERIC_READ),
                    null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);

            try (InputStream inputStream = smbFile.getInputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
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
     * @return 处理结果，包含文件列表或错误信息
     */
    @Override
    public Message list() {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            List<String> files = share.list(prefix).stream()
                    .filter(fileInfo -> !fileInfo.getFileName().equals(".") && !fileInfo.getFileName().equals(".."))
                    .map(fileInfo -> fileInfo.getFileName()).collect(Collectors.toList());

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(files.stream().map(fileName -> {
                        Map<String, Object> extend = new HashMap<>();
                        return Material.builder().name(fileName).extend(extend).build();
                    }).collect(Collectors.toList())).build();
        } catch (Exception e) {
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

            if (!share.fileExists(oldObjectKey)) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("File not found").build();
            }

            // 使用 smbj 的正确方式重命名文件
            // 1. 打开文件
            DiskEntry diskEntry = share.open(oldObjectKey, EnumSet.of(AccessMask.GENERIC_WRITE, AccessMask.DELETE),
                    EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL), SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OPEN, EnumSet.of(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE));

            try {
                // 2. 设置重命名信息 - 修正构造函数参数顺序
                FileRenameInformation renameInfo = new FileRenameInformation(false, // replaceIfExists - 不替换目标文件（如果存在）
                        0L, // rootDirectory - 根目录文件ID（通常为0）
                        newObjectKey // fileName - 新文件名
                );

                // 3. 应用重命名
                diskEntry.setFileInformation(renameInfo);

                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .build();
            } finally {
                diskEntry.close();
            }
        } catch (Exception e) {
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

            // 确保目录存在
            ensureDirectoryExists(dirPath);

            com.hierynomus.smbj.share.File smbFile = share.openFile(objectKey, EnumSet.of(AccessMask.GENERIC_WRITE),
                    null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OVERWRITE_IF, null);

            try (OutputStream outputStream = smbFile.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = content.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(Material.builder().name(fileName).path(objectKey).build()).build();
        } catch (Exception e) {
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
            if (share.fileExists(objectKey)) {
                share.rm(objectKey);
            }
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (Exception e) {
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
     * 从 endpoint 解析服务器地址，确保不包含端口信息。
     *
     * @param endpoint SMB 服务器地址，格式如 smb://hostname/share 或 hostname/share
     * @return 服务器地址
     */
    private String parseServerFromEndpoint(String endpoint) {
        if (StringKit.isBlank(endpoint)) {
            return "";
        }
        // 移除协议头（如 smb://, cifs://）
        String server = endpoint.replaceFirst("^(smb|cifs)://", "");
        // 移除共享名称和路径
        int slashIndex = server.indexOf('/');
        if (slashIndex != -1) {
            server = server.substring(0, slashIndex);
        }
        // 移除端口信息
        int colonIndex = server.indexOf(':');
        if (colonIndex != -1) {
            server = server.substring(0, colonIndex);
        }
        return server;
    }

    /**
     * 从 endpoint 解析端口。
     *
     * @param endpoint SMB 服务器地址，格式如 smb://hostname:port/share 或 hostname/share
     * @return 端口号，默认为 445
     */
    private int parsePortFromEndpoint(String endpoint) {
        if (StringKit.isBlank(endpoint)) {
            return 445; // SMB 默认端口
        }
        try {
            // 移除协议头（如 smb://, cifs://）
            String server = endpoint.replaceFirst("^(smb|cifs)://", "");
            // 提取端口部分
            int colonIndex = server.indexOf(':');
            int slashIndex = server.indexOf('/');

            if (colonIndex != -1) {
                String portStr = server.substring(colonIndex + 1, slashIndex != -1 ? slashIndex : server.length());
                return Integer.parseInt(portStr);
            }
        } catch (NumberFormatException e) {
            Logger.warn("Invalid port in endpoint: {}. Using default port 445.", endpoint);
        }
        return 445; // SMB 默认端口
    }

    /**
     * 从 endpoint 解析域名。
     *
     * @param endpoint SMB 服务器地址，格式如 smb://domain;hostname/share 或 hostname/share
     * @return 域名，可为空
     */
    private String parseDomainFromEndpoint(String endpoint) {
        if (StringKit.isBlank(endpoint)) {
            return "";
        }
        // 移除协议头（如 smb://, cifs://）
        String server = endpoint.replaceFirst("^(smb|cifs)://", "");
        // 检查是否包含域名
        int semicolonIndex = server.indexOf(';');
        if (semicolonIndex != -1) {
            return server.substring(0, semicolonIndex);
        }
        return "";
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

    /**
     * 确保目录存在，如果不存在则创建。
     *
     * @param dirPath 目录路径
     */
    private void ensureDirectoryExists(String dirPath) {
        if (StringKit.isBlank(dirPath) || dirPath.equals(Symbol.SLASH)) {
            return; // 根目录不需要创建
        }

        try {
            if (!share.folderExists(dirPath)) {
                // 创建父目录
                String parentPath = dirPath.substring(0, dirPath.lastIndexOf(Symbol.SLASH));
                if (!StringKit.isBlank(parentPath) && !parentPath.equals(Symbol.SLASH)) {
                    ensureDirectoryExists(parentPath);
                }
                // 创建当前目录
                share.mkdir(dirPath);
            }
        } catch (Exception e) {
            Logger.error("Failed to ensure directory exists: {}. Error: {}", dirPath, e.getMessage(), e);
            throw new InternalException("Failed to create directory: " + dirPath, e);
        }
    }

    /**
     * 检查文件是否存在。
     *
     * @param path 文件路径
     * @return 是否存在
     */
    private boolean isExist(String path) {
        try {
            return share.fileExists(path);
        } catch (Exception e) {
            Logger.error("Failed to check existence of file: {}. Error: {}", path, e.getMessage(), e);
            return false;
        }
    }

}