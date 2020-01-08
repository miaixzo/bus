/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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

import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.metric.Context;
import org.aoju.bus.metric.consts.MetricConsts;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 客户端传来的参数放在这里.
 *
 * @author Kimi Liu
 * @version 5.5.1
 * @since JDK 1.8++
 */
public class ApiParam extends JSONObject implements Param {

    private static final long serialVersionUID = 1L;
    private boolean ignoreSign;
    private boolean ignoreValidate;
    private String restName;
    private String restVersion;

    public ApiParam(Map<String, Object> map) {
        super(map);
    }

    public static String buildNameVersion(String name, String version) {
        if (StringUtils.isEmpty(version)) {
            return name;
        } else {
            return name + version;
        }
    }

    /**
     * 获取sign，并从param中删除
     *
     * @return 返回sign内容
     */
    public String fatchSignAndRemove() {
        String sign = this.fatchSign();
        this.remove(ParamNames.SIGN_NAME);
        return sign;
    }

    public HttpServletRequest fatchRequest() {
        return Context.getRequest();
    }

    /**
     * 是否忽略验证签名
     *
     * @return 返回true，忽略签名
     */
    public boolean fatchIgnoreSign() {
        return ignoreSign;
    }

    public void setIgnoreSign(boolean ignoreSign) {
        this.ignoreSign = ignoreSign;
    }

    public boolean fatchIgnoreValidate() {
        return ignoreValidate;
    }

    public void setIgnoreValidate(boolean ignoreValidate) {
        this.ignoreValidate = ignoreValidate;
    }

    /**
     * 接口名,如:goods.list
     */
    @Override
    public String fatchName() {
        String name = getString(ParamNames.API_NAME);
        if (name == null) {
            name = this.restName;
        }
        return name;
    }

    public void setName(String name) {
        this.restName = name;
    }

    public String fatchNameVersion() {
        return buildNameVersion(this.fatchName(), this.fatchVersion());
    }

    /**
     * 版本号
     */
    @Override
    public String fatchVersion() {
        String version = getString(ParamNames.VERSION_NAME);
        if (version == null) {
            version = this.restVersion;
        }
        return version;
    }

    public void setVersion(String version) {
        this.restVersion = version;
    }

    /**
     * 接入应用ID
     */
    @Override
    public String fatchAppKey() {
        return getString(ParamNames.APP_KEY_NAME);
    }

    public void setAppKey(String appKey) {
        put(ParamNames.APP_KEY_NAME, appKey);
    }

    /**
     * 参数,urlencode后的
     */
    @Override
    public String fatchData() {
        return getString(ParamNames.DATA_NAME);
    }

    public void setData(String json) {
        put(ParamNames.DATA_NAME, json);
    }

    /**
     * 时间戳，格式为yyyy-MM-dd HH:mm:ss，例如：2015-01-01 12:00:00
     */
    @Override
    public String fatchTimestamp() {
        return getString(ParamNames.TIMESTAMP_NAME);
    }

    public void setTimestamp(String timestamp) {
        put(ParamNames.TIMESTAMP_NAME, timestamp);
    }

    /**
     * 签名串
     */
    @Override
    public String fatchSign() {
        return getString(ParamNames.SIGN_NAME);
    }

    public void setSign(String sign) {
        put(ParamNames.SIGN_NAME, sign);
    }

    @Override
    public String fatchFormat() {
        String format = getString(ParamNames.FORMAT_NAME);
        if (format == null || "".equals(format)) {
            return MetricConsts.FORMAT_JSON;
        }
        return format;
    }

    public void setFormat(String format) {
        put(ParamNames.FORMAT_NAME, format);
    }

    @Override
    public String fatchAccessToken() {
        return getString(ParamNames.ACCESS_TOKEN_NAME);
    }

    @Override
    public String fatchSignMethod() {
        String signMethod = getString(ParamNames.SIGN_METHOD_NAME);
        if (signMethod == null) {
            return MetricConsts.DEFAULT_SIGN_METHOD;
        } else {
            return signMethod;
        }
    }

    @Override
    public ApiParam clone() {
        ApiParam param = new ApiParam(this);
        param.ignoreSign = this.ignoreSign;
        param.ignoreValidate = this.ignoreValidate;
        return param;
    }

}
