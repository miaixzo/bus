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

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.ErrorCode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 本地文件上传
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LocalFileProvider extends AbstractProvider {

    public LocalFileProvider(Context context) {
        this.context = context;
        Assert.notBlank(this.context.getRegion(), "[region] not defined");
    }

    @Override
    public Message download(String fileName) {
        return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).errmsg(ErrorCode.SUCCESS.getDesc())
                .data(new File(context.getRegion() + Symbol.SLASH + fileName)).build();
    }

    @Override
    public Message download(String bucket, String fileName) {
        return download(context.getRegion() + Symbol.SLASH + bucket + Symbol.SLASH + fileName);
    }

    @Override
    public Message download(String bucket, String fileName, File file) {
        return null;
    }

    @Override
    public Message download(String fileName, File file) {
        return null;
    }

    @Override
    public Message rename(String oldName, String newName) {
        return null;
    }

    @Override
    public Message rename(String bucket, String oldName, String newName) {
        return null;
    }

    @Override
    public Message upload(String fileName, byte[] content) {
        return null;
    }

    @Override
    public Message upload(String bucket, String fileName, InputStream content) {
        try {
            File dest = new File(context.getRegion() + Symbol.SLASH + bucket + Symbol.SLASH, fileName);
            if (!new File(dest.getParent()).exists()) {
                boolean result = new File(dest.getParent()).mkdirs();
                if (!result) {
                    return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc())
                            .build();
                }
            }
            OutputStream out = Files.newOutputStream(dest.toPath());
            IoKit.copy(content, out);
            content.close();
            out.close();
            return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).errmsg(ErrorCode.SUCCESS.getDesc()).build();
        } catch (IOException e) {
            Logger.error("file upload failed", e.getMessage());
        }
        return Message.builder().errcode(ErrorCode.FAILURE.getCode()).errmsg(ErrorCode.FAILURE.getDesc()).build();
    }

    @Override
    public Message upload(String bucket, String fileName, byte[] content) {
        return null;
    }

    @Override
    public Message remove(String fileName) {
        return null;
    }

    @Override
    public Message remove(String bucket, String fileName) {
        return null;
    }

    @Override
    public Message remove(String bucket, Path path) {
        return null;
    }

}
