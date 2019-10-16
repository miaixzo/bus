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
package org.aoju.bus.storage.provider;

import com.UpYun;
import com.upyun.UpException;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.storage.Builder;
import org.aoju.bus.storage.Context;
import org.aoju.bus.storage.magic.Attachs;
import org.aoju.bus.storage.magic.Readers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 存储服务-又拍云
 *
 * @author Kimi Liu
 * @version 5.0.3
 * @since JDK 1.8+
 */
public class UpaiYunOssProvider extends AbstractProvider {

    private UpYun client;

    public UpaiYunOssProvider(Context context) {
        this.context = context;
        Assert.notBlank(this.context.getPrefix(), "[prefix] not defined");
        Assert.notBlank(this.context.getEndpoint(), "[endpoint] not defined");
        Assert.notBlank(this.context.getBucket(), "[bucket] not defined");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] not defined");
        Assert.notBlank(this.context.getSecretKey(), "[secure] not defined");

        this.client = new UpYun(this.context.getBucket(), this.context.getAccessKey(), this.context.getSecretKey());
    }

    @Override
    public Readers download(String fileName) {
        return download(this.context.getBucket(), fileName);
    }

    @Override
    public Readers download(String bucket, String fileName) {
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers download(String fileName, File file) {
        try {
            this.client.writeFile(fileName, file);
            return new Readers(Builder.SUCCESS);
        } catch (IOException | UpException e) {
            Logger.error("file download failed" + e.getMessage());
        }
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers download(String bucket, String fileName, File file) {
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers list() {
        try {
            List<UpYun.FolderItem> list = client.readDir(this.context.getPrefix());
            return new Readers(list.stream().map(item -> {
                Attachs storageItem = new Attachs();
                storageItem.setName(item.name);
                storageItem.setType(item.type);
                storageItem.setSize(StringUtils.toString(item.size));
                return storageItem;
            }).collect(Collectors.toList()));
        } catch (IOException | UpException e) {
            Logger.error("file list failed" + e.getMessage());
        }
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
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers upload(String bucket, String fileName, byte[] content) {
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers remove(String fileName) {
        try {
            client.deleteFile("/" + fileName);
            return new Readers(Builder.SUCCESS);
        } catch (IOException | UpException e) {
            Logger.error("file remove failed", e.getMessage());
        }
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers remove(String bucket, String fileName) {
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers remove(String bucket, Path path) {
        return remove(bucket, path.toString());
    }

}
