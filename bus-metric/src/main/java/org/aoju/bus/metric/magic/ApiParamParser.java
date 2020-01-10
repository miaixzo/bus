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
package org.aoju.bus.metric.magic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.metric.ApiContext;
import org.aoju.bus.metric.consts.MetricConsts;
import org.aoju.bus.metric.support.RequestUtil;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 参数解析默认实现
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8++
 */
public class ApiParamParser implements ParamParser {

    public static final String UPLOAD_FORM_DATA_NAME = "body_data";
    private static final String CONTENT_TYPE_MULTIPART = MediaType.MULTIPART_FORM_DATA_VALUE;
    private static final String CONTENT_TYPE_JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String CONTENT_TYPE_TEXT = MediaType.TEXT_PLAIN_VALUE;
    private static String REQUEST_DATA_NAME = ParamNames.DATA_NAME;


    @Override
    public ApiParam parse(HttpServletRequest request) {
        String requestJson = null;
        try {
            requestJson = this.getJson(request);
        } catch (Exception e) {
            Logger.error("parse error", e);
        }

        if (StringUtils.isEmpty(requestJson)) {
            throw Errors.ERROR_PARAM.getException();
        }

        if (ApiContext.hasUseNewSSL(request)) {
            requestJson = this.decryptData(requestJson);
        } else if (ApiContext.isEncryptMode()) {
            String randomKey = ApiContext.getRandomKey();
            if (randomKey == null) {
                Logger.error("未找到randomKey");
                throw Errors.ERROR_SSL.getException();
            }
            requestJson = ApiContext.decryptAES(requestJson);
        }
        ApiParam param = this.jsonToApiParam(requestJson);
        this.bindRestParam(param, request);
        return param;
    }

    private String decryptData(String requestJson) {
        JSONObject result = JSON.parseObject(requestJson);
        String data = result.getString(REQUEST_DATA_NAME);
        try {
            // aes解密
            data = ApiContext.decryptAESFromBase64String(data);
            // 重新赋值
            result.put(REQUEST_DATA_NAME, data);
        } catch (Exception e) {
            throw new RuntimeException("AES解密失败");
        }
        return result.toJSONString();
    }

    public String getJson(HttpServletRequest request) throws Exception {
        String requestJson;

        if (RequestUtil.isGetRequest(request)) {
            Map<String, Object> params = RequestUtil.convertRequestParamsToMap(request);
            requestJson = JSON.toJSONString(params);
        } else {
            String contectType = request.getContentType();

            if (contectType == null) {
                contectType = "";
            }

            contectType = contectType.toLowerCase();

            // json或者纯文本形式
            if (contectType.contains(CONTENT_TYPE_JSON) || contectType.contains(CONTENT_TYPE_TEXT)) {
                requestJson = RequestUtil.getText(request);
            } else if (contectType.contains(CONTENT_TYPE_MULTIPART)) {
                // 上传文件形式
                requestJson = this.parseUploadRequest(request);
            } else {
                Map<String, Object> params = RequestUtil.convertRequestParamsToMap(request);
                requestJson = JSON.toJSONString(params);
            }
        }

        return requestJson;
    }

    /**
     * 解析文件上传请求
     *
     * @param request 请求
     * @return 返回json字符串
     */
    protected String parseUploadRequest(HttpServletRequest request) {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext());
        // 检查form中是否有enctype="multipart/form-data"
        if (multipartResolver.isMultipart(request)) {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            Map<String, MultipartFile> fileMap = multiRequest.getFileMap();
            Map<String, MultipartFile> finalMap = new HashMap<>(fileMap.size());

            Set<String> keys = fileMap.keySet();
            for (String name : keys) {
                MultipartFile file = fileMap.get(name);
                if (file.getSize() > 0) {
                    finalMap.put(name, file);
                }
            }

            if (finalMap.size() > 0) {
                // 保存上传文件
                ApiContext.setUploadContext(new ApiUpload(finalMap));
            }
        }

        String json = request.getParameter(UPLOAD_FORM_DATA_NAME);
        if (json != null) {
            return json;
        }

        Map<String, Object> params = RequestUtil.convertRequestParamsToMap(request);
        return JSON.toJSONString(params);
    }

    protected ApiParam jsonToApiParam(String json) {
        if (StringUtils.isEmpty(json)) {
            throw Errors.ERROR_PARAM.getException();
        }

        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(json);
        } catch (Exception e) {
            throw Errors.ERROR_JSON_DATA.getException(e.getMessage());
        }

        return new ApiParam(jsonObject);
    }

    protected void bindRestParam(ApiParam param, HttpServletRequest request) {
        String name = (String) request.getAttribute(MetricConsts.REST_PARAM_NAME);
        String version = (String) request.getAttribute(MetricConsts.REST_PARAM_VERSION);
        if (name != null) {
            param.setName(name);
        }
        if (version != null) {
            param.setVersion(version);
        }
    }

}
