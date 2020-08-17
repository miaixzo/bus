/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.metric;

import lombok.Data;
import org.aoju.bus.core.Version;
import org.aoju.bus.metric.builtin.ConfigClient;
import org.aoju.bus.metric.manual.*;
import org.aoju.bus.metric.manual.docs.DocFileCreator;
import org.aoju.bus.metric.secure.*;
import org.aoju.bus.metric.session.ApiSessionManager;
import org.aoju.bus.metric.session.SessionManager;
import org.aoju.bus.metric.support.JsonResultSerializer;
import org.aoju.bus.metric.support.XmlResultSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 配置类,所有配置相关都在这里.
 *
 * @author Kimi Liu
 * @version 6.0.6
 * @since JDK 1.8++
 */
@Data
public class ApiConfig {

    private String name = "metric";

    private String version = Version.all();
    /**
     * 加密工具
     */
    private Safety safety = new ApiSafety();
    /**
     * 签名工具
     */
    private Signer signer = new ApiSigner();
    private Writer writer = new ApiWriter();
    /**
     * 请求转发
     */
    private Invoker invoker = new ApiInvoker();
    /**
     * 校验接口
     */
    private Validator validator = new ApiValidator();
    /**
     * 参数解析
     */
    private ParamParser paramParser = new ApiParamParser();
    /**
     * 错误模块
     */
    private List<String> isvModules = new ArrayList<>();
    /**
     * data部分解码
     */
    private DataDecoder dataDecoder = new ApiDataDecoder();
    /**
     * 负责监控的拦截器
     */
    private ApiHandler monitorInerceptor = new MonitorHandler();
    /**
     * 存储监控信息
     */
    private MonitorStore monitorStore = new ApiMonitorStore();
    /**
     * app秘钥管理
     */
    private AppSecretManager appSecretManager = new CacheAppSecretManager();
    /**
     * xml序列化
     */
    private ResultSerializer xmlResultSerializer = new XmlResultSerializer();
    /**
     * json序列化
     */
    private ResultSerializer jsonResultSerializer = new JsonResultSerializer();
    /**
     * 返回结果
     */
    private ResultCreator resultCreator = new ApiResultCreator();
    /**
     * session管理
     */
    private SessionManager sessionManager = new ApiSessionManager();
    /**
     * api注册事件
     */
    private ApiRegistEvent apiRegistEvent = apiDefinition -> {
    };

    /**
     * oauth2服务端默认实现
     */
    private Oauth2Service oauth2Service;
    /**
     * oauth2认证服务，需要自己实现
     */
    private Oauth2Manager oauth2Manager;
    /**
     * 文档管理
     */
    private DocFileCreator docFileCreator;

    private ConfigClient configClient;
    /**
     * 最外包装类class
     */
    private Class<? extends Result> wrapperClass;
    /**
     * 拦截器
     */
    private ApiHandler[] interceptors = {};
    /**
     * 权限管理
     */
    private PermissionManager permissionManager;
    /**
     * 超时时间
     */
    private int timeoutSeconds = 3;
    /**
     * 是否生成doc文档
     */
    private boolean showDoc;
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
     * 是否开启监控
     */
    private boolean showMonitor = true;
    /**
     * 存放监控错误信息队列长度。超出长度，新值替换旧值
     */
    private int monitorErrorQueueSize = 5;
    /**
     * 处理线程池大小
     */
    private int monitorExecutorSize = 2;

    /**
     * markdown文档保存目录
     */
    private String markdownDocDir;

    public ApiConfig() {
        isvModules.add("i18n/open/error");
    }

    /**
     * 开启app对接模式，开启后不进行timeout校验
     * 如果平台直接跟Android或IOS对接，可开启这个功能。因为手机上的时间有可能跟服务端的时间不一致(用户的手机情况不可控)
     * 失去了时间校验，一个请求有可能被反复调用，服务端需要防止重复提交，有必要的话上HTTPS。
     */
    public void openAppMode() {
        this.timeoutSeconds = 0;
    }

    public void initOauth2Service(Oauth2Manager oauth2Manager) {
        this.oauth2Manager = oauth2Manager;
        this.oauth2Service.setOauth2Manager(oauth2Manager);
    }

    /**
     * 添加秘钥配置，map中存放秘钥信息，key对应appKey，value对应secret
     *
     * @param appSecretStore 秘钥信息
     */
    public void addAppSecret(Map<String, String> appSecretStore) {
        this.appSecretManager.addAppSecret(appSecretStore);
    }

}
