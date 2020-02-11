/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.storage.provider;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.storage.Builder;
import org.aoju.bus.storage.Context;
import org.aoju.bus.storage.magic.Attachs;
import org.aoju.bus.storage.magic.Readers;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;

/**
 * 存储服务-七牛
 *
 * @author Kimi Liu
 * @version 5.5.8
 * @since JDK 1.8+
 */
public class QiniuYunOssProvider extends AbstractProvider {

    private UploadManager uploadManager;
    private BucketManager bucketManager;
    private Auth auth;

    public QiniuYunOssProvider(Context context) {
        this.context = context;
        Assert.notBlank(this.context.getPrefix(), "[prefix] not defined");
        Assert.notBlank(this.context.getBucket(), "[bucket] not defined");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] not defined");
        Assert.notBlank(this.context.getSecretKey(), "[secretKey] not defined");
        Assert.notBlank(this.context.getRegion(), "[region] not defined");
        Assert.notNull(this.context.isSecure(), "[secure] not defined");

        this.auth = Auth.create(this.context.getAccessKey(), this.context.getSecretKey());

        Region region = Region.autoRegion(this.context.getRegion());
        Configuration c = new Configuration(region);
        this.uploadManager = new UploadManager(c);
        this.bucketManager = new BucketManager(auth, c);
    }

    @Override
    public Readers download(String fileKey) {
        String path = getFullPath(fileKey);
        if (this.context.isSecure()) {
            path = this.auth.privateDownloadUrl(path, 3600);
        }
        try {
            String encodedFileName = URLEncoder.encode(fileKey, "utf-8");
            String format = String.format("%s/%s", path, encodedFileName);
            return new Readers(Builder.SUCCESS);
        } catch (UnsupportedEncodingException e) {
            Logger.error("file download failed", e.getMessage());
        }
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers download(String bucket, String fileName) {
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers download(String bucket, String fileName, File file) {
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers download(String fileName, File file) {
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers list() {
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers rename(String oldName, String newName) {
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers rename(String bucket, String oldName, String newName) {
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers upload(String fileName, byte[] content) {
        return upload(this.context.getBucket(), fileName, content);
    }

    @Override
    public Readers upload(String bucket, String fileName, InputStream content) {
        try {
            String upToken = auth.uploadToken(bucket);
            Response response = uploadManager.put(content, fileName, upToken, null, null);
            if (!response.isOK()) {
                return new Readers(Builder.FAILURE);
            }
            return new Readers(Attachs.builder()
                    .name(fileName)
                    .size(StringUtils.toString(response.body().length))
                    .path(response.url()).build());
        } catch (QiniuException e) {
            Logger.error("file upload failed", e.getMessage());
        }
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers upload(String bucket, String fileName, byte[] content) {
        try {
            String upToken = auth.uploadToken(bucket, fileName);
            Response response = uploadManager.put(content, fileName, upToken);
            if (!response.isOK()) {
                return new Readers(Builder.FAILURE);
            }
            return new Readers(Attachs.builder()
                    .size(StringUtils.toString(response.body().length))
                    .name(fileName)
                    .path(response.url()).build());
        } catch (QiniuException e) {
            Logger.error("file upload failed", e.getMessage());
        }
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers remove(String fileKey) {
        try {
            if (fileKey.contains(Symbol.SLASH)) {
                fileKey = fileKey.replace(this.context.getPrefix(), Normal.EMPTY);
            }
            bucketManager.delete(this.context.getBucket(), fileKey);
            return new Readers(Builder.SUCCESS);
        } catch (QiniuException e) {
            Logger.error("file remove failed", e.getMessage());
        }
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers remove(String bucket, String fileName) {
        try {
            bucketManager.delete(bucket, fileName);
            return new Readers(Builder.SUCCESS);
        } catch (QiniuException e) {
            Logger.error("file remove failed", e.getMessage());
        }
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers remove(String bucket, Path path) {
        return remove(bucket, path.toString());
    }

}
