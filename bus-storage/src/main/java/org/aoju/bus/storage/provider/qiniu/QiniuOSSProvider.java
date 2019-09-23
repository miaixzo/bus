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
package org.aoju.bus.storage.provider.qiniu;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.Data;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.MapUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.storage.Provider;
import org.aoju.bus.storage.UploadObject;
import org.aoju.bus.storage.UploadToken;
import org.aoju.bus.storage.provider.AbstractProvider;

import java.util.Map;

/**
 * @author Kimi Liu
 * @version 3.5.3
 * @since JDK 1.8
 */
public class QiniuOSSProvider extends AbstractProvider {

    private static final String DEFAULT_CALLBACK_BODY = "filename=${fname}&size=${fsize}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}";

    private static final String[] policyFields = new String[]{
            "callbackUrl",
            "callbackBody",
            "callbackHost",
            "callbackBodyType",
            "fileType",
            "saveKey",
            "mimeLimit",
            "fsizeLimit",
            "fsizeMin",
            "deleteAfterDays",
    };

    private static UploadManager uploadManager;
    private static BucketManager bucketManager;
    private Auth auth;
    private String host;

    public QiniuOSSProvider(String prefix, String bucket, String accessKey, String secretKey, boolean privated) {
        Assert.notBlank(bucket, "[bucket] not defined");
        Assert.notBlank(accessKey, "[accessKey] not defined");
        Assert.notBlank(secretKey, "[secretKey] not defined");
        Assert.notBlank(prefix, "[urlprefix] not defined");

        this.prefix = prefix.endsWith(DIR_SPLITER) ? prefix : prefix + DIR_SPLITER;
        this.bucket = bucket;
        this.auth = Auth.create(accessKey, secretKey);

        Region region = Region.autoRegion();
        Configuration c = new Configuration(region);
        uploadManager = new UploadManager(c);
        bucketManager = new BucketManager(auth, c);

        this.privated = privated;
        this.host = StringUtils.remove(prefix, "/").split(":")[1];
    }

    @Override
    public String upload(UploadObject object) {
        String fileName = object.getFileName();
        if (StringUtils.isNotBlank(object.getCatalog())) {
            fileName = object.getCatalog().concat("/").concat(fileName);
        }
        try {
            Response res;
            String upToken = getUpToken();
            if (object.getFile() != null) {
                res = uploadManager.put(object.getFile(), fileName, upToken);
            } else if (object.getBytes() != null) {
                res = uploadManager.put(object.getBytes(), fileName, upToken);
            } else if (object.getInputStream() != null) {
                res = uploadManager.put(object.getInputStream(), fileName, upToken, null, object.getMimeType());
            } else if (StringUtils.isNotBlank(object.getUrl())) {
                return bucketManager.fetch(object.getUrl(), bucket, fileName).key;
            } else {
                throw new IllegalArgumentException("upload object is NULL");
            }
            return processUploadResponse(res);
        } catch (QiniuException e) {
            throw new InstrumentException(e);
        }
    }

    @Override
    public String getUrl(String fileKey, boolean isInternal) {
        String path = getFullPath(fileKey);
        if (this.privated) {
            path = this.auth.privateDownloadUrl(path, 3600);
        }
        return path;
    }

    @Override
    public boolean delete(String fileKey) {
        try {
            if (fileKey.contains(DIR_SPLITER)) {
                fileKey = fileKey.replace(this.prefix, "");
            }
            bucketManager.delete(bucket, fileKey);
            return true;
        } catch (QiniuException e) {
            throw new InstrumentException(e);
        }
    }

    @Override
    public Map<String, Object> createUploadToken(UploadToken param) {

        if (StringUtils.isNotBlank(param.getCallbackUrl())) {
            if (StringUtils.isBlank(param.getCallbackBody())) {
                param.setCallbackBody(DEFAULT_CALLBACK_BODY);
            }
            if (StringUtils.isBlank(param.getCallbackHost())) {
                param.setCallbackHost(this.host);
            }
        }

        Map<String, Object> result = MapUtils.newHashMap();
        StringMap policy = new StringMap();
        policy.putNotNull(policyFields[0], param.getCallbackUrl());
        policy.putNotNull(policyFields[1], param.getCallbackBody());
        policy.putNotNull(policyFields[2], param.getCallbackHost());
        policy.putNotNull(policyFields[3], param.getCallbackBodyType());
        policy.putNotNull(policyFields[4], param.getFileType());
        policy.putNotNull(policyFields[5], param.getFileKey());
        policy.putNotNull(policyFields[6], param.getMimeLimit());
        policy.putNotNull(policyFields[7], param.getFsizeMin());
        policy.putNotNull(policyFields[8], param.getFsizeMax());
        policy.putNotNull(policyFields[9], param.getDeleteAfterDays());

        String token = auth.uploadToken(this.bucket, param.getFileKey(), param.getExpires(), policy, true);
        result.put("uptoken", token);
        result.put("host", this.prefix);
        result.put("dir", param.getUploadDir());

        return result;
    }

    @Override
    public void close() {
    }

    @Override
    public String name() {
        return Provider.QINIU_OSS.getValue();
    }


    /**
     * 处理上传结果，返回文件url
     *
     * @return
     * @throws QiniuException
     */
    private String processUploadResponse(Response res) throws QiniuException {
        if (res.isOK()) {
            UploadResult ret = res.jsonToObject(UploadResult.class);
            return getFullPath(ret.getKey());
        }
        throw new InstrumentException(res.toString());
    }

    private String getUpToken() {
        return auth.uploadToken(bucket);
    }

    @Data
    class UploadResult {

        private long fsize;
        private String key;
        private String hash;
        private int width;
        private int height;

    }

}
