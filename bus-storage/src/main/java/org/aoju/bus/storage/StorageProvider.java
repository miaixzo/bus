/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.storage;

import java.io.Closeable;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 3.6.1
 * @since JDK 1.8
 */
public interface StorageProvider extends Closeable {

    String name();

    /**
     * 文件上传
     *
     * @param object 上传对象
     * @return the string
     */
    String upload(UploadObject object);

    /**
     * 获取文件下载地址
     *
     * @param fileKey    文件标识
     * @param isInternal 是否内部
     * @return the string
     */
    String getUrl(String fileKey, boolean isInternal);

    /**
     * 删除图片
     *
     * @param fileKey 文件标识
     * @return the true/false
     */
    boolean delete(String fileKey);

    String downloadAndSaveAs(String fileKey, String localSaveDir, boolean isInternal);

    Map<String, Object> createUploadToken(UploadToken param);
}
