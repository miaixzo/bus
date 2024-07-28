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

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.ErrorCode;
import org.miaixz.bus.storage.magic.Material;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;

/**
 * 存储服务-七牛
 *
 * @author Kimi Liu
 * @since Java 17+
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
    public Message download(String fileKey) {
        String path = getFullPath(fileKey);
        if (this.context.isSecure()) {
            path = this.auth.privateDownloadUrl(path, 3600);
        }
        try {
            String encodedFileName = URLEncoder.encode(fileKey, Charset.DEFAULT_UTF_8);
            String format = String.format("%s/%s", path, encodedFileName);
            // FileKit.newFile(format);
            return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).errmsg(ErrorCode.SUCCESS.getDesc()).build();
        } catch (UnsupportedEncodingException e) {
            Logger.error("file download failed", e.getMessage());
        }
        return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
    }

    @Override
    public Message download(String bucket, String fileName) {
        return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
    }

    @Override
    public Message download(String bucket, String fileName, File file) {
        return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
    }

    @Override
    public Message download(String fileName, File file) {
        return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
    }

    @Override
    public Message rename(String oldName, String newName) {
        return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
    }

    @Override
    public Message rename(String bucket, String oldName, String newName) {
        return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
    }

    @Override
    public Message upload(String fileName, byte[] content) {
        return upload(this.context.getBucket(), fileName, content);
    }

    @Override
    public Message upload(String bucket, String fileName, InputStream content) {
        try {
            String upToken = auth.uploadToken(bucket);
            Response response = uploadManager.put(content, fileName, upToken, null, null);
            if (!response.isOK()) {
                return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc())
                        .build();
            }
            return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).errmsg(ErrorCode.SUCCESS.getDesc())
                    .data(Material.builder().name(fileName).size(StringKit.toString(response.body().length))
                            .path(response.url()))
                    .build();
        } catch (QiniuException e) {
            Logger.error("file upload failed", e.getMessage());
        }
        return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
    }

    @Override
    public Message upload(String bucket, String fileName, byte[] content) {
        try {
            String upToken = auth.uploadToken(bucket, fileName);
            Response response = uploadManager.put(content, fileName, upToken);
            if (!response.isOK()) {
                return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc())
                        .build();
            }
            return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).errmsg(ErrorCode.SUCCESS.getDesc())
                    .data(Material.builder().size(StringKit.toString(response.body().length)).name(fileName)
                            .path(response.url()))
                    .build();
        } catch (QiniuException e) {
            Logger.error("file upload failed", e.getMessage());
        }
        return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
    }

    @Override
    public Message remove(String fileKey) {
        try {
            if (fileKey.contains(Symbol.SLASH)) {
                fileKey = fileKey.replace(this.context.getPrefix(), Normal.EMPTY);
            }
            bucketManager.delete(this.context.getBucket(), fileKey);
            return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).errmsg(ErrorCode.SUCCESS.getDesc()).build();
        } catch (QiniuException e) {
            Logger.error("file remove failed", e.getMessage());
        }
        return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
    }

    @Override
    public Message remove(String bucket, String fileName) {
        try {
            bucketManager.delete(bucket, fileName);
            return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).errmsg(ErrorCode.SUCCESS.getDesc()).build();
        } catch (QiniuException e) {
            Logger.error("file remove failed", e.getMessage());
        }
        return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
    }

    @Override
    public Message remove(String bucket, Path path) {
        return remove(bucket, path.toString());
    }

}
