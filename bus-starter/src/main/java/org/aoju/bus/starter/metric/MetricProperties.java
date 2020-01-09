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
package org.aoju.bus.starter.metric;

import org.aoju.bus.starter.BusXExtend;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * API网关配置信息
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8++
 */
@ConfigurationProperties(prefix = BusXExtend.METRIC)
public class MetricProperties {

    private static final String DEFAULT_APP_NAME = "app";

    private static String CONFIG_FOLDER = System.getProperty("user.dir") + File.separator + "local-config" + File.separator;
    /**
     * app,secret键值对
     */
    private Map<String, String> appSecret = Collections.emptyMap();
    /**
     * 拦截器
     */
    private List<String> interceptors = Collections.emptyList();
    /**
     * 消息模块
     */
    private List<String> isvModules = Collections.emptyList();
    /**
     * 应用名称
     */
    private String appName;
    /**
     * 默认版本号
     */
    private String defaultVersion = "";
    /**
     * 超时时间
     */
    private int timeoutSeconds = 3;
    /**
     * 是否生成doc文档
     */
    private boolean showDoc;
    /**
     * 文档页面密码，默认为null，如果不为null，文档页面一定开启。
     */
    private String docPassword;
    /**
     * 文档模板路径
     */
    private String docClassPath = "/easyopen_template/doc.html";
    /**
     * 文档下载模板路径
     */
    private String docPdfClassPath = "/easyopen_template/docPdf.html";
    private String docPdfCssClassPath = "/easyopen_template/docPdf.css";
    /**
     * 监控模板路径
     */
    private String monitorClassPath = "/easyopen_template/monitor.html";
    /**
     * 登录页模板路径
     */
    private String loginClassPath = "/easyopen_template/login.html";
    /**
     * 限流 模板路径
     */
    private String limitClassPath = "/easyopen_template/limit.html";
    /**
     * 进入限流页面密码
     */
    private String limitPassword = "limit123";
    /**
     * 默认每秒可处理请求数
     */
    private int defaultLimitCount = 50;
    /**
     * 默认令牌桶个数
     */
    private int defaultTokenBucketCount = 50;
    /**
     * 本地限流缓存全路径
     */
    private String localLimitConfigFile;
    /**
     * 本地权限缓存全路径
     */
    private String localPermissionConfigFile;
    /**
     * 本地秘钥缓存全路径
     */
    private String localSecretConfigFile;
    /**
     * 忽略验证
     */
    private boolean ignoreValidate;
    /**
     * 登录视图页面用于，mvc视图，如：loginView
     */
    private String oauth2LoginUri = "/oauth2login";
    /**
     * oauth2的accessToken过期时间,单位秒,默认2小时
     */
    private long oauth2ExpireIn = 7200;
    /**
     * jwt过期时间,秒,默认2小时
     */
    private int jwtExpireIn = 7200;
    /**
     * RSA加密对应的私钥
     */
    private String privateKey;
    /**
     * 私钥文件存放的classpath地址
     */
    private String priKeyPath = "/pri.key";
    /**
     * 是否开启监控
     */
    private boolean showMonitor = true;
    /**
     * 进入监控页面密码
     */
    private String monitorPassword = "monitor123";
    /**
     * 存放监控错误信息队列长度。超出长度，新值替换旧值
     */
    private int monitorErrorQueueSize = 5;
    /**
     * 处理线程池大小
     */
    private int monitorExecutorSize = 2;
    /**
     * 配置中心ip
     */
    private String configServerIp;
    /**
     * 配置中心端口号
     */
    private String configServerPort;
    /**
     * 文档页面url
     */
    private String docUrl;
    /**
     * 是否启用分布式限流
     */
    private boolean configDistributedLimit;
    /**
     * markdown文档保存目录
     */
    private String markdownDocDir;
    /**
     * 设置跨域，为true时开启跨域。默认true
     */
    private String cors = "true";
    /**
     * 设置true，开启webflux。默认false
     */
    private String mono;

    public MetricProperties() {
        this.setAppName(DEFAULT_APP_NAME);
    }

    //=======================================

    public List<String> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(List<String> interceptors) {
        this.interceptors = interceptors;
    }

    public List<String> getIsvModules() {
        return isvModules;
    }

    public void setIsvModules(List<String> isvModules) {
        this.isvModules = isvModules;
    }

    public String getConfigServerIp() {
        return configServerIp;
    }

    public void setConfigServerIp(String configServerIp) {
        this.configServerIp = configServerIp;
    }

    public String getConfigServerPort() {
        return configServerPort;
    }

    public void setConfigServerPort(String configServerPort) {
        this.configServerPort = configServerPort;
    }

    public boolean isConfigDistributedLimit() {
        return configDistributedLimit;
    }

    public void setConfigDistributedLimit(boolean configDistributedLimit) {
        this.configDistributedLimit = configDistributedLimit;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
        /** 本地限流缓存全路径 */
        localLimitConfigFile = System.getProperty("conflimit.file", CONFIG_FOLDER + appName + "-limit.json");
        /** 本地权限缓存全路径 */
        localPermissionConfigFile = System.getProperty("confperm.file", CONFIG_FOLDER + appName + "-permission.json");
        /** 本地秘钥缓存全路径 */
        localSecretConfigFile = System.getProperty("confsecret.file", CONFIG_FOLDER + appName + "-secret.json");
    }

    public Map<String, String> getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(Map<String, String> appSecret) {
        this.appSecret = appSecret;
    }

    public String getDefaultVersion() {
        return defaultVersion;
    }

    public void setDefaultVersion(String defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isShowDoc() {
        return showDoc;
    }

    public void setShowDoc(boolean showDoc) {
        this.showDoc = showDoc;
    }

    public String getDocPassword() {
        return docPassword;
    }

    public void setDocPassword(String docPassword) {
        this.docPassword = docPassword;
    }

    public String getDocClassPath() {
        return docClassPath;
    }

    public void setDocClassPath(String docClassPath) {
        this.docClassPath = docClassPath;
    }

    public String getDocPdfClassPath() {
        return docPdfClassPath;
    }

    public void setDocPdfClassPath(String docPdfClassPath) {
        this.docPdfClassPath = docPdfClassPath;
    }

    public String getDocPdfCssClassPath() {
        return docPdfCssClassPath;
    }

    public void setDocPdfCssClassPath(String docPdfCssClassPath) {
        this.docPdfCssClassPath = docPdfCssClassPath;
    }

    public String getMonitorClassPath() {
        return monitorClassPath;
    }

    public void setMonitorClassPath(String monitorClassPath) {
        this.monitorClassPath = monitorClassPath;
    }

    public String getLoginClassPath() {
        return loginClassPath;
    }

    public void setLoginClassPath(String loginClassPath) {
        this.loginClassPath = loginClassPath;
    }

    public String getLimitClassPath() {
        return limitClassPath;
    }

    public void setLimitClassPath(String limitClassPath) {
        this.limitClassPath = limitClassPath;
    }

    public String getLimitPassword() {
        return limitPassword;
    }

    public void setLimitPassword(String limitPassword) {
        this.limitPassword = limitPassword;
    }

    public int getDefaultLimitCount() {
        return defaultLimitCount;
    }

    public void setDefaultLimitCount(int defaultLimitCount) {
        this.defaultLimitCount = defaultLimitCount;
    }

    public int getDefaultTokenBucketCount() {
        return defaultTokenBucketCount;
    }

    public void setDefaultTokenBucketCount(int defaultTokenBucketCount) {
        this.defaultTokenBucketCount = defaultTokenBucketCount;
    }

    public String getLocalLimitConfigFile() {
        return localLimitConfigFile;
    }

    public void setLocalLimitConfigFile(String localLimitConfigFile) {
        this.localLimitConfigFile = localLimitConfigFile;
    }

    public String getLocalPermissionConfigFile() {
        return localPermissionConfigFile;
    }

    public void setLocalPermissionConfigFile(String localPermissionConfigFile) {
        this.localPermissionConfigFile = localPermissionConfigFile;
    }

    public String getLocalSecretConfigFile() {
        return localSecretConfigFile;
    }

    public void setLocalSecretConfigFile(String localSecretConfigFile) {
        this.localSecretConfigFile = localSecretConfigFile;
    }

    public boolean isIgnoreValidate() {
        return ignoreValidate;
    }

    public void setIgnoreValidate(boolean ignoreValidate) {
        this.ignoreValidate = ignoreValidate;
    }

    public String getOauth2LoginUri() {
        return oauth2LoginUri;
    }

    public void setOauth2LoginUri(String oauth2LoginUri) {
        this.oauth2LoginUri = oauth2LoginUri;
    }

    public long getOauth2ExpireIn() {
        return oauth2ExpireIn;
    }

    public void setOauth2ExpireIn(long oauth2ExpireIn) {
        this.oauth2ExpireIn = oauth2ExpireIn;
    }

    public int getJwtExpireIn() {
        return jwtExpireIn;
    }

    public void setJwtExpireIn(int jwtExpireIn) {
        this.jwtExpireIn = jwtExpireIn;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPriKeyPath() {
        return priKeyPath;
    }

    public void setPriKeyPath(String priKeyPath) {
        this.priKeyPath = priKeyPath;
    }

    public boolean isShowMonitor() {
        return showMonitor;
    }

    public void setShowMonitor(boolean showMonitor) {
        this.showMonitor = showMonitor;
    }

    public String getMonitorPassword() {
        return monitorPassword;
    }

    public void setMonitorPassword(String monitorPassword) {
        this.monitorPassword = monitorPassword;
    }

    public int getMonitorErrorQueueSize() {
        return monitorErrorQueueSize;
    }

    public void setMonitorErrorQueueSize(int monitorErrorQueueSize) {
        this.monitorErrorQueueSize = monitorErrorQueueSize;
    }

    public int getMonitorExecutorSize() {
        return monitorExecutorSize;
    }

    public void setMonitorExecutorSize(int monitorExecutorSize) {
        this.monitorExecutorSize = monitorExecutorSize;
    }

    public String getDocUrl() {
        return docUrl;
    }

    public void setDocUrl(String docUrl) {
        this.docUrl = docUrl;
    }

    public String getMarkdownDocDir() {
        return markdownDocDir;
    }

    public void setMarkdownDocDir(String markdownDocDir) {
        this.markdownDocDir = markdownDocDir;
    }

    public String getCors() {
        return cors;
    }

    public void setCors(String cors) {
        this.cors = cors;
    }

    public String getMono() {
        return mono;
    }

    public void setMono(String mono) {
        this.mono = mono;
    }

}
