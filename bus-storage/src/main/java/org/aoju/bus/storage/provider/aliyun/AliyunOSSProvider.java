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
package org.aoju.bus.storage.provider.aliyun;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.*;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.DateUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.storage.Provider;
import org.aoju.bus.storage.UploadObject;
import org.aoju.bus.storage.UploadToken;
import org.aoju.bus.storage.provider.AbstractProvider;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 3.6.0
 * @since JDK 1.8
 */
public class AliyunOSSProvider extends AbstractProvider {

    private static final String URL_PREFIX_PATTERN = "(http).*\\.(com|cn)\\/";
    private static final String DEFAULT_CALLBACK_BODY = "filename=${object}&size=${size}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}";

    private OSSClient client;
    private String internalUrl;

    public AliyunOSSProvider(String urlPrefix, String endpoint, String bucket, String accessKey, String secretKey, String internalUrl, boolean privated) {
        Assert.notBlank(endpoint, "[endpoint] not defined");
        Assert.notBlank(bucket, "[bucketName] not defined");
        Assert.notBlank(accessKey, "[accessKey] not defined");
        Assert.notBlank(secretKey, "[secretKey] not defined");
        Assert.notBlank(urlPrefix, "[urlprefix] not defined");
        this.internalUrl = internalUrl;
        this.accessKey = accessKey;
        this.client = new OSSClient(endpoint, accessKey, secretKey);
        this.bucket = bucket;
        this.prefix = urlPrefix.endsWith("/") ? urlPrefix : (urlPrefix + "/");
        this.privated = privated;
        if (!this.client.doesBucketExist(bucket)) {
            System.out.println("Creating bucket " + bucket + "\n");
            this.client.createBucket(bucket);
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucket);
            createBucketRequest.setCannedACL(privated ? CannedAccessControlList.Private : CannedAccessControlList.PublicRead);
            this.client.createBucket(createBucketRequest);
        }
    }

    @Override
    public String upload(UploadObject object) {
        try {
            PutObjectRequest request;
            if (object.getFile() != null) {
                request = new PutObjectRequest(bucket, object.getFileName(), object.getFile());
            } else if (object.getBytes() != null) {
                request = new PutObjectRequest(bucket, object.getFileName(), new ByteArrayInputStream(object.getBytes()));
            } else if (object.getInputStream() != null) {
                request = new PutObjectRequest(bucket, object.getFileName(), object.getInputStream());
            } else {
                throw new IllegalArgumentException("upload object is NULL");
            }

            PutObjectResult result = this.client.putObject(request);
            if (result.getResponse() == null) {
                return privated ? object.getFileName() : prefix + object.getFileName();
            }
            if (result.getResponse().isSuccessful()) {
                return result.getResponse().getUri();
            } else {
                throw new InstrumentException(result.getResponse().getErrorResponseAsString());
            }
        } catch (OSSException e) {
            throw new InstrumentException(e.getErrorMessage());
        }
    }

    @Override
    public Map<String, Object> createUploadToken(UploadToken param) {
        try {
            Map<String, Object> result = new HashMap<>();
            PolicyConditions policyConds = new PolicyConditions();
            if (param.getFsizeMin() != null && param.getFsizeMax() != null) {
                policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, param.getFsizeMin(), param.getFsizeMax());
            } else {
                policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            }
            if (param.getUploadDir() != null) {
                policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, param.getUploadDir());
            }
            if (StringUtils.isBlank(param.getCallbackHost())) {
                param.setCallbackHost(StringUtils.remove(this.prefix, "/").split(":")[1]);
            }
            if (StringUtils.isBlank(param.getCallbackBody())) {
                param.setCallbackBody(DEFAULT_CALLBACK_BODY);
            }

            Date expire = DateUtils.addSeconds(new Date(), (int) param.getExpires());


            String policy = this.client.generatePostPolicy(expire, policyConds);

            String policyBase64 = BinaryUtil.toBase64String(policy.getBytes(StandardCharsets.UTF_8.name()));
            String callbackBase64 = param.getCallbackRuleAsJson();
            if (StringUtils.isNotEmpty(callbackBase64)) {
                callbackBase64 = BinaryUtil.toBase64String(callbackBase64.getBytes(StandardCharsets.UTF_8.name()));
            }

            result.put("OSSAccessKeyId", accessKey);
            result.put("policy", policyBase64);
            result.put("signature", this.client.calculatePostSignature(policy));
            result.put("host", this.prefix);
            result.put("dir", param.getUploadDir());
            result.put("expire", String.valueOf(expire.getTime()));
            if (StringUtils.isNotEmpty(callbackBase64)) {
                result.put("callback", callbackBase64);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(String fileKey) {
        this.client.deleteObject(bucket, fileKey);
        return true;
    }

    @Override
    public String getUrl(String fileKey, boolean isInternal) {
        String visitedUrl;
        if (privated) {
            URL url = this.client.generatePresignedUrl(bucket, fileKey, DateUtils.addHours(new Date(), 1));
            visitedUrl = url.toString();
        } else {
            visitedUrl = prefix + fileKey;
        }

        if (isInternal) {
            visitedUrl = visitedUrl.replaceFirst(URL_PREFIX_PATTERN, internalUrl);
        }
        return visitedUrl;
    }

    @Override
    public void close() {
        this.client.shutdown();
    }

    @Override
    public String name() {
        return Provider.ALI_OSS.getValue();
    }

}
