/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.storage;

import org.miaixz.bus.storage.magic.Message;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * 文件存储提供者
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Provider {

    /**
     * 文件下载(流式下载)
     *
     * @param fileName 文件名
     * @return 处理结果 {@link Message}
     */
    Message download(String fileName);

    /**
     * 文件下载(流式下载)
     *
     * @param bucket   存储桶名
     * @param fileName 文件名
     * @return 处理结果 {@link Message}
     */
    Message download(String bucket, String fileName);

    /**
     * 文件下载(文件下载到本地)
     *
     * @param bucket   存储桶名
     * @param fileName 文件名
     * @param file     保存的本地文件路径
     * @return 处理结果 {@link Message}
     */
    Message download(String bucket, String fileName, File file);

    /**
     * 文件下载(文件下载到本地)
     *
     * @param fileName 文件名
     * @param file     保存的本地文件路径
     * @return 处理结果 {@link Message}
     */
    Message download(String fileName, File file);

    /**
     * 文件列表
     *
     * @return 处理结果 {@link Message}
     */
    default Message list() {
        return Message.builder()
                .errcode(Builder.ErrorCode.FAILURE.getCode())
                .errmsg(Builder.ErrorCode.FAILURE.getMsg())
                .build();
    }

    /**
     * 重命名
     *
     * @param oldName 原始名称
     * @param newName 新名称
     * @return 处理结果 {@link Message}
     */
    Message rename(String oldName, String newName);

    /**
     * 重命名
     *
     * @param bucket  存储桶名
     * @param oldName 原始名称
     * @param newName 新名称
     * @return 处理结果 {@link Message}
     */
    Message rename(String bucket, String oldName, String newName);

    /**
     * 上传文件到指定的 bucket
     *
     * @param fileName 文件名字
     * @param content  文件内容
     * @return 处理结果 {@link Message}
     */
    Message upload(String fileName, byte[] content);

    /**
     * 上传文件到指定的 bucket
     *
     * @param bucket   存储桶名
     * @param fileName 文件名字
     * @param content  文件内容
     * @return 处理结果 {@link Message}
     */
    Message upload(String bucket, String fileName, InputStream content);

    /**
     * 上传文件到指定的 bucket
     *
     * @param bucket   存储桶名
     * @param fileName 文件名字
     * @param content  文件内容
     * @return 处理结果 {@link Message}
     */
    Message upload(String bucket, String fileName, byte[] content);

    /**
     * 删除文件
     *
     * @param fileName 文件名
     * @return 处理结果 {@link Message}
     */
    Message remove(String fileName);

    /**
     * 删除文件
     *
     * @param bucket   存储桶名
     * @param fileName 文件名
     * @return 处理结果 {@link Message}
     */
    Message remove(String bucket, String fileName);

    /**
     * 删除文件
     *
     * @param bucket 存储桶名
     * @param path   文件路径
     * @return 处理结果 {@link Message}
     */
    Message remove(String bucket, Path path);

}
