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

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.metric.builtin.ConfigClient;
import org.aoju.bus.metric.consts.MetricConsts;
import org.aoju.bus.metric.consts.RequestMode;
import org.aoju.bus.metric.handler.InvokeHandler;
import org.aoju.bus.metric.handler.ResponseHandler;
import org.aoju.bus.metric.manual.Errors;
import org.aoju.bus.metric.manual.Result;
import org.aoju.bus.metric.manual.Writer;
import org.aoju.bus.metric.register.AbstractInitializer;
import org.aoju.bus.metric.secure.Oauth2Manager;
import org.aoju.bus.metric.secure.Oauth2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 提供API访问能力,新建一个类继承这个即可.RequestMapping中的value自己定义
 *
 * <pre>
 * &#64;Controller
 * &#64;RequestMapping(value = "/api")
 * public class IndexController extends ApiController {
 * }
 *
 * 这样接口的统一访问路径为:http://ip:port/contextPath/api
 * </pre>
 *
 * @author Kimi Liu
 * @version 6.0.6
 * @since JDK 1.8++
 */
public abstract class ApiRouter extends AbstractInitializer
        implements ApplicationListener<ContextRefreshedEvent>, ResponseHandler {

    @Autowired
    protected Oauth2Manager oauth2Manager;
    @Autowired
    protected Oauth2Service oauth2Service;
    @Autowired
    protected ApiAware apiAware;

    protected InvokeHandler invokeHandler;
    protected volatile ApiConfig apiConfig;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (ObjectKit.isEmpty(apiAware.getApplicationContext())) {
            apiAware.setApplicationContext(event.getApplicationContext());
        }
        this.onStartup();
    }

    @Override
    public void onRegistFinished(ApiConfig apiConfig) {
        ConfigClient configClient = this.apiConfig.getConfigClient();
        if (configClient != null) {
            configClient.init();
        }
    }

    /**
     * 初始化apiConfig对象。spring容器加载完毕后触发此方法，因此方法中可以使用被@Autowired注解的对象。
     *
     * @param apiConfig 配置
     */
    protected abstract void initApiConfig(ApiConfig apiConfig);

    protected synchronized void onStartup() {
        if (this.apiConfig != null) {
            return;
        }
        // 新建一个ApiConfig
        this.apiConfig = ApiContext.getConfig();
        ApiContext.setConfig(apiConfig);

        this.invokeHandler = new InvokeHandler(apiConfig, this);
        // 初始化配置
        this.initApiConfig(this.apiConfig);
        // easyopen初始化工作，注册接口
        this.init(this.apiConfig);
        // 初始化其它组件
        // 放在最后
        if (oauth2Manager != null) {
            apiConfig.initOauth2Service(oauth2Manager);
            oauth2Service = apiConfig.getOauth2Service();
        }

        if (this.apiConfig.isShowMonitor()) {
            ApiHandler[] interceptors = this.apiConfig.getInterceptors();
            int len = interceptors.length + 1;
            ApiHandler[] newInterceptors = new ApiHandler[len];
            // 监控拦截器放在首位
            newInterceptors[0] = this.apiConfig.getMonitorInerceptor();
            for (int i = 0; i < interceptors.length; i++) {
                newInterceptors[i + 1] = interceptors[i];
            }
            this.apiConfig.setInterceptors(newInterceptors);
        }
    }

    /**
     * 请求入口
     *
     * @param request  请求
     * @param response 响应
     */
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.GET})
    public void rest(HttpServletRequest request, HttpServletResponse response) {
        // 调用接口方法,即调用被@Api标记的方法
        ApiContext.setRequestMode(RequestMode.SIGNATURE);
        this.invokeHandler.processInvoke(request, response);
    }

    /**
     * 接口名体现在地址栏：
     * http://www.xxx.com/api/接口名/
     * http://www.xxx.com/api/goods.get/
     * <strong>注意：必须要以斜杠“/”结尾</strong>
     *
     * @param name     接口名
     * @param request  请求
     * @param response 响应
     */
    @RequestMapping(value = "/{name}/", method = {RequestMethod.POST, RequestMethod.GET})
    public void rest(@PathVariable("name") String name,
                     HttpServletRequest request, HttpServletResponse response) {
        this.router(name, null, request, response);
    }

    /**
     * 接口名和版本号体现在地址栏：
     * http://www.xxx.com/api/接口名/版本号/
     * http://www.xxx.com/api/goods.get/1.0/
     * <strong>注意：必须要以斜杠“/”结尾</strong>
     *
     * @param name     接口名
     * @param version  版本号
     * @param request  请求
     * @param response 响应
     */
    @RequestMapping(value = "/{name}/{version}/", method = {RequestMethod.POST, RequestMethod.GET})
    public void rest(@PathVariable("name") String name, @PathVariable("version") String version,
                     HttpServletRequest request, HttpServletResponse response) {
        this.router(name, version, request, response);
    }

    protected void router(String name, String version,
                          HttpServletRequest request, HttpServletResponse response) {
        if (name == null) {
            throw new IllegalArgumentException("name不能为空");
        }
        if (version == null) {
            version = this.apiConfig.getVersion();
        }
        request.setAttribute(MetricConsts.REST_PARAM_NAME, name);
        request.setAttribute(MetricConsts.REST_PARAM_VERSION, version);
        this.rest(request, response);
    }

    /**
     * 写数据到客户端
     *
     * @param response 响应
     * @param result   结果
     */
    @Override
    public void responseResult(HttpServletRequest request, HttpServletResponse response, Object result) {
        if (result == null) {
            return;
        }
        Writer respWriter = this.apiConfig.getWriter();
        respWriter.write(request, response, result);
    }

    @Override
    public Result caugthException(Throwable e) {
        String code = Errors.SYS_ERROR.getCode();
        String msg = e.getMessage();
        Object data = null;

        if (e instanceof InstrumentException) {
            InstrumentException apiEx = (InstrumentException) e;
            code = apiEx.getErrcode();
            msg = apiEx.getMessage();
        }

        return this.apiConfig.getResultCreator().createErrorResult(code, msg, data);
    }

}
