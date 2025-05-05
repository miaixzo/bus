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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.ErrorCode;

import com.UpYun;
import com.upyun.UpException;

/**
 * 存储服务-又拍云
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UpyunOssProvider extends AbstractProvider {

    private volatile UpYun client;

    public UpyunOssProvider(Context context) {
        this.context = context;

        Assert.notBlank(this.context.getBucket(), "[bucket] not defined");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] not defined");
        Assert.notBlank(this.context.getSecretKey(), "[secure] not defined");

        this.client = new UpYun(this.context.getBucket(), this.context.getAccessKey(), this.context.getSecretKey());
    }

    @Override
    public Message download(String fileName) {
        return download(this.context.getBucket(), fileName);
    }

    @Override
    public Message download(String bucket, String fileName) {
        return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
    }

    @Override
    public Message download(String fileName, File file) {
        try {
            this.client.writeFile(fileName, file);
            return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).errmsg(ErrorCode.SUCCESS.getDesc()).build();
        } catch (IOException | UpException e) {
            Logger.error("file download failed" + e.getMessage());
        }
        return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
    }

    @Override
    public Message download(String bucket, String fileName, File file) {
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
        return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
    }

    @Override
    public Message upload(String bucket, String fileName, byte[] content) {
        return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
    }

    @Override
    public Message remove(String fileName) {
        try {
            client.deleteFile(Symbol.C_SLASH + fileName, null);
            return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).errmsg(ErrorCode.SUCCESS.getDesc()).build();
        } catch (IOException | UpException e) {
            Logger.error("file remove failed {}", e.getMessage());
        }
        return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
    }

    @Override
    public Message remove(String bucket, String fileName) {
        return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
    }

    @Override
    public Message remove(String bucket, Path path) {
        return remove(bucket, path.toString());
    }

}
