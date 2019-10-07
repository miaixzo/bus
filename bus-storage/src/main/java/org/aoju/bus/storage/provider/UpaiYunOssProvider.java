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
 * @version 3.6.5
 * @since JDK 1.8
 */
public class UpaiYunOssProvider extends AbstractProvider {

    private UpYun client;

    public UpaiYunOssProvider(Context property) {
        this.property = property;
        Assert.notBlank(this.property.getPrefix(), "[prefix] not defined");
        Assert.notBlank(this.property.getEndpoint(), "[endpoint] not defined");
        Assert.notBlank(this.property.getBucket(), "[bucket] not defined");
        Assert.notBlank(this.property.getAccessKey(), "[accessKey] not defined");
        Assert.notBlank(this.property.getSecretKey(), "[secure] not defined");

        this.client = new UpYun(this.property.getBucket(), this.property.getAccessKey(), this.property.getSecretKey());
    }

    @Override
    public Readers download(String fileName) {
        return download(this.property.getBucket(), fileName);
    }

    @Override
    public Readers download(String bucket, String fileName) {
        return new Readers(null, "failure to provide services");
    }

    @Override
    public Readers download(String fileName, File file) {
        try {
            this.client.writeFile(fileName, file);
            return new Readers(null, Builder.SUCCESS);
        } catch (IOException | UpException e) {
            Logger.error("file download failed" + e.getMessage());
        }
        return new Readers(null, Builder.FAILURE);
    }

    @Override
    public Readers download(String bucket, String fileName, File file) {
        return new Readers(null, "failure to provide services");
    }

    @Override
    public Readers list() {
        try {
            List<UpYun.FolderItem> list = client.readDir(this.property.getPrefix());
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
        return new Readers(null, Builder.FAILURE);
    }

    @Override
    public Readers rename(String oldName, String newName) {
        return new Readers(null, "failure to provide services");
    }

    @Override
    public Readers rename(String bucket, String oldName, String newName) {
        return new Readers(null, "failure to provide services");
    }

    @Override
    public Readers upload(String fileName, byte[] content) {
        return upload(this.property.getBucket(), fileName, content);
    }

    @Override
    public Readers upload(String bucket, String fileName, InputStream content) {
        return new Readers(null, "failure to provide services");
    }

    @Override
    public Readers upload(String bucket, String fileName, byte[] content) {
        return new Readers(null, "failure to provide services");
    }

    @Override
    public Readers remove(String fileName) {
        try {
            client.deleteFile("/" + fileName);
            return new Readers(Builder.SUCCESS);
        } catch (IOException | UpException e) {
            Logger.error("file remove failed", e.getMessage());
        }
        return new Readers(null, Builder.FAILURE);
    }

    @Override
    public Readers remove(String bucket, String fileName) {
        return new Readers(null, "failure to provide services");
    }

    @Override
    public Readers remove(String bucket, Path path) {
        return remove(bucket, path.toString());
    }

}
