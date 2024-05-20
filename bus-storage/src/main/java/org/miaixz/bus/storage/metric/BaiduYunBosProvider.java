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
package org.miaixz.bus.storage.metric;

import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.baidubce.services.bos.model.GetObjectRequest;
import com.baidubce.services.bos.model.ListObjectsRequest;
import com.baidubce.services.bos.model.ListObjectsResponse;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.storage.Builder;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.Message;
import org.miaixz.bus.storage.magic.Property;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 存储服务-百度云
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BaiduYunBosProvider extends AbstractProvider {

    private BosClient client;

    public BaiduYunBosProvider(Context context) {
        this.context = context;
        Assert.notBlank(this.context.getPrefix(), "[prefix] not defined");
        Assert.notBlank(this.context.getEndpoint(), "[endpoint] not defined");
        Assert.notBlank(this.context.getBucket(), "[bucket] not defined");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] not defined");
        Assert.notBlank(this.context.getSecretKey(), "[secure] not defined");
        Assert.notNull(this.context.isSecure(), "[secure] not defined");

        BosClientConfiguration config = new BosClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(this.context.getAccessKey(), this.context.getSecretKey()));
        config.setEndpoint(this.context.getEndpoint());
        this.client = new BosClient(config);
    }

    @Override
    public Message download(String fileName) {
        return download(this.context.getBucket(), fileName);
    }

    @Override
    public Message download(String bucket, String fileName) {
        return Message.builder()
                .errcode(Builder.ErrorCode.FAILURE.getCode())
                .errmsg(Builder.ErrorCode.FAILURE.getMsg()).build();
    }

    @Override
    public Message download(String fileName, File file) {
        return download(this.context.getBucket(), fileName, file);
    }

    @Override
    public Message download(String bucket, String fileName, File file) {
        this.client.getObject(new GetObjectRequest(bucket, fileName), file);

        return Message.builder()
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .errmsg(Builder.ErrorCode.SUCCESS.getMsg()).build();
    }

    @Override
    public Message list() {
        ListObjectsRequest request = new ListObjectsRequest(this.context.getBucket());
        ListObjectsResponse objectListing = this.client.listObjects(request);

        return Message.builder()
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .errmsg(Builder.ErrorCode.SUCCESS.getMsg())
                .data(objectListing.getContents().stream().map(item -> {
                    Property storageItem = new Property();
                    storageItem.setName(item.getKey());
                    storageItem.setOwner(item.getOwner().getDisplayName());
                    storageItem.setSize(StringKit.toString(item.getSize()));
                    Map<String, Object> extend = new HashMap<>();
                    extend.put("tag", item.getETag());
                    extend.put("storageClass", item.getStorageClass());
                    extend.put("lastModified", item.getLastModified());
                    storageItem.setExtend(extend);
                    return storageItem;
                }).collect(Collectors.toList()))
                .build();
    }

    @Override
    public Message rename(String oldName, String newName) {
        return Message.builder()
                .errcode(Builder.ErrorCode.FAILURE.getCode())
                .errmsg(Builder.ErrorCode.FAILURE.getMsg()).build();
    }

    @Override
    public Message rename(String bucket, String oldName, String newName) {
        return Message.builder()
                .errcode(Builder.ErrorCode.FAILURE.getCode())
                .errmsg(Builder.ErrorCode.FAILURE.getMsg()).build();
    }

    @Override
    public Message upload(String fileName, byte[] content) {
        return upload(this.context.getBucket(), fileName, content);
    }

    @Override
    public Message upload(String bucket, String fileName, InputStream content) {
        this.client.putObject(bucket, fileName, content);
        return Message.builder()
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .errmsg(Builder.ErrorCode.SUCCESS.getMsg()).build();
    }

    @Override
    public Message upload(String bucket, String fileName, byte[] content) {
        this.client.putObject(bucket, fileName, content);
        return Message.builder()
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .errmsg(Builder.ErrorCode.SUCCESS.getMsg()).build();
    }

    @Override
    public Message remove(String fileName) {
        return remove(this.context.getBucket(), fileName);
    }

    @Override
    public Message remove(String bucket, String fileName) {
        client.deleteObject(bucket, fileName);
        return Message.builder()
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .errmsg(Builder.ErrorCode.SUCCESS.getMsg()).build();
    }

    @Override
    public Message remove(String bucket, Path path) {
        return remove(bucket, path.toString());
    }

}
