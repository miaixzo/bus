package org.aoju.bus.storage.provider.aliyun;

import org.aoju.bus.core.utils.DateUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.storage.UploadObject;
import org.aoju.bus.storage.UploadToken;
import org.aoju.bus.storage.provider.AbstractProvider;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class AliyunossProvider extends AbstractProvider {

    public static final String NAME = "aliyun";

    private static final String URL_PREFIX_PATTERN = "(http).*\\.(com|cn)\\/";
    private static final String DEFAULT_CALLBACK_BODY = "filename=${object}&size=${size}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}";

    private OSSClient ossClient;
    private String bucketName;
    private String urlprefix;
    private boolean isPrivate;
    private String accessKeyId;
    private String host;

    public AliyunossProvider(String urlprefix, String endpoint, String bucketName, String accessKey, String secretKey, boolean isPrivate) {
        this.accessKeyId = accessKey;
        ossClient = new OSSClient(endpoint, accessKey, secretKey);
        this.bucketName = bucketName;
        this.urlprefix = urlprefix.endsWith("/") ? urlprefix : (urlprefix + "/");
        this.isPrivate = isPrivate;

        this.host = urlprefix.replace(urlprefix, "/").split(":")[1];
        if (!ossClient.doesBucketExist(bucketName)) {
            ossClient.createBucket(bucketName);
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
            createBucketRequest.setCannedACL(isPrivate ? CannedAccessControlList.Private : CannedAccessControlList.PublicRead);
            ossClient.createBucket(createBucketRequest);
        }
    }

    @Override
    public String upload(UploadObject object) {
        try {
            PutObjectRequest request = null;
            if (object.getFile() != null) {
                request = new PutObjectRequest(bucketName, object.getFileName(), object.getFile());
            } else if (object.getBytes() != null) {
                request = new PutObjectRequest(bucketName, object.getFileName(), new ByteArrayInputStream(object.getBytes()));
            } else if (object.getInputStream() != null) {
                request = new PutObjectRequest(bucketName, object.getFileName(), object.getInputStream());
            } else {
                throw new IllegalArgumentException("upload object is NULL");
            }

            PutObjectResult result = ossClient.putObject(request);
            if (result.getResponse() == null) {
                return isPrivate ? object.getFileName() : urlprefix + object.getFileName();
            }
            if (result.getResponse().isSuccessful()) {
                return result.getResponse().getUri();
            } else {
                throw new RuntimeException(result.getResponse().getErrorResponseAsString());
            }
        } catch (OSSException e) {
            throw new RuntimeException(e.getErrorMessage());
        }
    }

    //https://help.aliyun.com/document_detail/31926.html
    //https://help.aliyun.com/document_detail/31989.html?spm=a2c4g.11186623.6.907.tlMQcL
    @Override
    public Map<String, Object> createUploadToken(UploadToken param) {

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
            param.setCallbackHost(host);
        }

        if (StringUtils.isBlank(param.getCallbackBody())) {
            param.setCallbackBody(DEFAULT_CALLBACK_BODY);
        }

        Date expire = DateUtils.addSeconds(new Date(), (int) param.getExpires());
        String policy = ossClient.generatePostPolicy(expire, policyConds);
        String policyBase64 = null;
        String callbackBase64 = null;
        try {
            policyBase64 = BinaryUtil.toBase64String(policy.getBytes(StandardCharsets.UTF_8.name()));
            String callbackJson = param.getCallbackRuleAsJson();
            if (callbackJson != null) {
                callbackBase64 = BinaryUtil.toBase64String(callbackJson.getBytes(StandardCharsets.UTF_8.name()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String signature = ossClient.calculatePostSignature(policy);

        result.put("OSSAccessKeyId", accessKeyId);
        result.put("policy", policyBase64);
        result.put("signature", signature);
        result.put("host", this.urlprefix);
        result.put("dir", param.getUploadDir());
        result.put("expire", String.valueOf(expire.getTime()));
        if (callbackBase64 != null) {
            result.put("callback", callbackBase64);
        }
        return result;
    }

    @Override
    public boolean delete(String fileKey) {
        ossClient.deleteObject(bucketName, fileKey);
        return true;
    }

    @Override
    public String getUrl(String fileKey) {
        //ObjectAcl objectAcl = ossClient.getObjectAcl(bucketName, key);
        if (isPrivate) {
            URL url = ossClient.generatePresignedUrl(bucketName, fileKey, DateUtils.addHours(new Date(), 1));
            return url.toString().replaceFirst(URL_PREFIX_PATTERN, urlprefix);
        }
        return urlprefix + fileKey;
    }

    @Override
    public void close() throws IOException {
        ossClient.shutdown();
    }

    @Override
    public String name() {
        return NAME;
    }

}
