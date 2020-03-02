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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.google.common.collect.Maps;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.storage.Builder;
import org.aoju.bus.storage.Context;
import org.aoju.bus.storage.magic.Attachs;
import org.aoju.bus.storage.magic.Readers;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 存储服务-京东云
 *
 * @author Kimi Liu
 * @version 5.6.5
 * @since JDK 1.8+
 */
public class JdYunOssProvider extends AbstractProvider {

    private AmazonS3 client;

    public JdYunOssProvider(Context context) {
        this.context = context;
        Assert.notBlank(this.context.getPrefix(), "[prefix] not defined");
        Assert.notBlank(this.context.getEndpoint(), "[endpoint] not defined");
        Assert.notBlank(this.context.getBucket(), "[bucket] not defined");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] not defined");
        Assert.notBlank(this.context.getSecretKey(), "[secure] not defined");
        Assert.notBlank(this.context.getRegion(), "[region] not defined");

        ClientConfiguration config = new ClientConfiguration();

        AwsClientBuilder.EndpointConfiguration endpointConfig =
                new AwsClientBuilder.EndpointConfiguration(this.context.getEndpoint(), this.context.getRegion());

        AWSCredentials awsCredentials = new BasicAWSCredentials(this.context.getAccessKey(), this.context.getSecretKey());
        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);

        client = AmazonS3Client.builder()
                .withEndpointConfiguration(endpointConfig)
                .withClientConfiguration(config)
                .withCredentials(awsCredentialsProvider)
                .disableChunkedEncoding()
                .withPathStyleAccessEnabled(true)
                .build();
    }

    @Override
    public Readers download(String fileName) {
        return download(this.context.getBucket(), fileName);
    }

    @Override
    public Readers download(String bucket, String fileName) {
        return new Readers(null, "failure to provide services");
    }

    @Override
    public Readers download(String fileName, File file) {
        return download(this.context.getBucket(), fileName, file);
    }

    @Override
    public Readers download(String bucket, String fileName, File file) {
        this.client.getObject(new GetObjectRequest(bucket, fileName), file);
        return new Readers(Builder.SUCCESS);
    }

    @Override
    public Readers list() {
        ListObjectsRequest request = new ListObjectsRequest().withBucketName(this.context.getBucket());
        ObjectListing objectListing = client.listObjects(request);
        return new Readers(objectListing.getObjectSummaries().stream().map(item -> {
            Attachs storageItem = new Attachs();
            storageItem.setName(item.getKey());
            storageItem.setOwner(item.getOwner().getDisplayName());
            storageItem.setSize(StringUtils.toString(item.getSize()));
            Map<String, Object> extend = Maps.newHashMap();
            extend.put("tag", item.getETag());
            extend.put("storageClass", item.getStorageClass());
            extend.put("lastModified", item.getLastModified());
            storageItem.setExtend(extend);
            return storageItem;
        }).collect(Collectors.toList()));
    }

    @Override
    public Readers rename(String oldName, String newName) {
        return new Readers(null, "failure to provide services");
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
        client.putObject(bucket, fileName, content, null);
        return new Readers(Builder.SUCCESS);
    }

    @Override
    public Readers upload(String bucket, String fileName, byte[] content) {
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers remove(String fileName) {
        return remove(this.context.getBucket(), fileName);
    }

    @Override
    public Readers remove(String bucket, String fileName) {
        this.client.deleteObject(bucket, fileName);
        return new Readers(Builder.SUCCESS);
    }

    @Override
    public Readers remove(String bucket, Path path) {
        return remove(bucket, path.toString());
    }

}
