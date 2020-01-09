package org.aoju.bus.metric.support;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.metric.ApiConfig;
import org.aoju.bus.metric.ApiContext;
import org.aoju.bus.metric.ApiHandler;
import org.aoju.bus.metric.builtin.ApiDocHolder;
import org.aoju.bus.metric.consts.MetricConsts;
import org.aoju.bus.metric.consts.RequestMode;
import org.aoju.bus.metric.handler.InvokeHandler;
import org.aoju.bus.metric.handler.ResponseHandler;
import org.aoju.bus.metric.magic.*;
import org.aoju.bus.metric.register.AbstractInitializer;
import org.aoju.bus.metric.secure.Oauth2Manager;
import org.aoju.bus.metric.secure.Oauth2Service;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

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
 * @author tanghc
 */
public abstract class ApiController extends AbstractInitializer
        implements ApplicationListener<ContextRefreshedEvent>, ResponseHandler {

    protected static volatile ApplicationContext ctx;
    protected volatile ApiConfig apiConfig;
    @Autowired
    protected Oauth2Manager oauth2Manager;
    @Autowired
    protected Oauth2Service oauth2Service;
    private InvokeHandler invokeHandler;

    /**
     * 初始化apiConfig对象。spring容器加载完毕后触发此方法，因此方法中可以使用被@Autowired注解的对象。
     *
     * @param apiConfig 配置
     */
    protected abstract void initApiConfig(ApiConfig apiConfig);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext appCtx = this.getApplicationContext();
        if (appCtx == null) {
            appCtx = event.getApplicationContext();
        }
        this.onStartup(appCtx);
    }

    protected ApplicationContext getApplicationContext() {
        return ctx;
    }

    protected synchronized void onStartup(ApplicationContext applicationContext) {
        if (this.apiConfig != null) {
            return;
        }
        // 保存ApplicationContext
        ApiContext.setApplicationContext(applicationContext);
        // 新建一个ApiConfig
        this.apiConfig = newApiConfig();
        ApiContext.setConfig(apiConfig);

        // this.apiConfig.loadPrivateKey();
        // 初始化Template
        this.initTemplate();
        // 初始化配置
        this.initApiConfig(this.apiConfig);
        // easyopen初始化工作，注册接口
        this.init(applicationContext, this.apiConfig);
        // 初始化其它组件
        // 放在最后
        this.initComponent();
    }

    @Override
    public void onRegistFinished(ApiConfig apiConfig) {
        /*ConfigClient configClient = this.apiConfig.getConfigClient();
        if (configClient != null) {
            configClient.init();
        } else {
            LimitConfigManager limitConfigManager = apiConfig.getLimitConfigManager();
            if (limitConfigManager != null) {
                limitConfigManager.loadToLocalCache();
            }
        }*/
    }

    protected void initTemplate() {
        /*this.invokeTemplate = new InvokeTemplate(apiConfig, this);
        this.webfluxInvokeTemplate = new WebfluxInvokeTemplate(apiConfig, this);
        this.docTemplate = new DocTemplate(apiConfig, this);
        this.monitorTemplate = new MonitorTemplate(apiConfig, this);
        this.limitTemplate = new LimitTemplate(apiConfig, this);
        this.handshakeTemplate = new HandshakeTemplate(apiConfig);*/
    }

    protected ApiConfig newApiConfig() {
        return ApiContext.getConfig();
    }

    private void initComponent() {
        if (oauth2Manager != null) {
            apiConfig.initOauth2Service(oauth2Manager);
            oauth2Service = apiConfig.getOauth2Service();
        }

        initInterceptor();
    }

    /**
     * 添加监控拦截器
     */
    private void initInterceptor() {
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
    public void index(HttpServletRequest request, HttpServletResponse response) {
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
        this.doRest(name, null, request, response);
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
    public void rest2(@PathVariable("name") String name, @PathVariable("version") String version,
                      HttpServletRequest request, HttpServletResponse response) {
        this.doRest(name, version, request, response);
    }

    protected void doRest(String name, String version,
                          HttpServletRequest request, HttpServletResponse response) {
        if (name == null) {
            throw new IllegalArgumentException("name不能为空");
        }
        if (version == null) {
            version = this.apiConfig.getVersion();
        }
        request.setAttribute(MetricConsts.REST_PARAM_NAME, name);
        request.setAttribute(MetricConsts.REST_PARAM_VERSION, version);
        this.index(request, response);
    }

    /**
     * Mock请求入口
     *
     * @param request  请求
     * @param response 响应
     */
    @RequestMapping(value = "mock", method = {RequestMethod.POST, RequestMethod.GET})
    public void indexMock(HttpServletRequest request, HttpServletResponse response) {
        // 调用接口方法,即调用被@Api标记的方法
        ApiContext.setRequestMode(RequestMode.SIGNATURE);
        // this.invokeTemplate.processInvokeMock(request, response);
    }

    /**
     * 加密请求入口
     *
     * @param request  请求
     * @param response 响应
     * @throws Throwable 异常
     */
    @RequestMapping(value = "ssl", method = RequestMethod.POST)
    public void ssl(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        ApiContext.setRequestMode(RequestMode.ENCRYPT);
        //  this.invokeTemplate.processInvoke(request, response);
    }

    /**
     * 加密请求入口
     *
     * @param request  请求
     * @param response 响应
     * @throws Throwable 异常
     */
    @RequestMapping(value = "ssl2", method = RequestMethod.POST)
    public void ssl2(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        ApiContext.useNewSSL(request);
        // this.invokeTemplate.processInvoke(request, response);
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

    /**
     * 文档页面
     *
     * @param request  请求
     * @param response 响应
     * @throws Throwable 异常
     */
    @RequestMapping("doc")
    public void doc(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        // this.docTemplate.processDoc(request, response);
    }

    /**
     * 文档下载
     *
     * @param request  请求
     * @param response 响应
     * @throws Throwable 异常
     */
    @RequestMapping("doc/download")
    public void docDl(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        // this.docTemplate.downloadPdf(request, response);
    }

    @RequestMapping("json/doc")
    @ResponseBody
    public Map jsondoc(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        Map context = new HashMap();
        context.put("apiModules", ApiDocHolder.getApiDocBuilder().getApiModules());

        context.put("ACCESS_TOKEN_NAME", ParamNames.ACCESS_TOKEN_NAME);
        context.put("API_NAME", ParamNames.API_NAME);
        context.put("APP_KEY_NAME", ParamNames.APP_KEY_NAME);
        context.put("DATA_NAME", ParamNames.DATA_NAME);
        context.put("FORMAT_NAME", ParamNames.FORMAT_NAME);
        context.put("SIGN_METHOD_NAME", ParamNames.SIGN_METHOD_NAME);
        context.put("SIGN_NAME", ParamNames.SIGN_NAME);
        context.put("TIMESTAMP_NAME", ParamNames.TIMESTAMP_NAME);
        context.put("TIMESTAMP_PATTERN", ParamNames.TIMESTAMP_PATTERN);
        context.put("VERSION_NAME", ParamNames.VERSION_NAME);

        context.put("code_name", "code");
        context.put("code_description", "状态值，\"0\"表示成功，其它都是失败");

        context.put("msg_name", "msg");
        context.put("msg_description", "错误信息，出错时显示");

        context.put("data_name", "data");
        context.put("data_description", "返回的数据，没有则返回{}");
        return context;
    }

    @RequestMapping("doc/pwd")
    @ResponseBody
    public Map docPwd(String password, HttpServletRequest request) throws Throwable {
        Map map = new HashMap();
        /*if (apiConfig.getDocPassword().equals(password)) {
            map.put("code", 0);
            map.put("msg", "验证成功");
            Map<String, String> data = new HashMap<>();
            data.put("id", "doc");
            data.put("username", RequestUtil.getClientIP(request));
            String jwt = ApiContext.createJwt(data);
            map.put("jwt", jwt);
        } else {
            map.put("code", 1);
            map.put("msg", "验证失败");
        }*/
        return map;
    }

    /**
     * 交换随机码
     *
     * @param request  请求
     * @param response 响应
     * @return 返回握手信息
     */
    @RequestMapping(value = "handshake", method = RequestMethod.POST)
    @ResponseBody
    public Object handshake(HttpServletRequest request, HttpServletResponse response) {
        return null; // return this.handshakeTemplate.handshake(request, response);
    }

    /**
     * 交换随机码
     *
     * @param request  请求
     * @param response 响应
     * @return 返回握手信息
     */
    @RequestMapping(value = "handshake2", method = RequestMethod.POST)
    @ResponseBody
    public Object handshake2(HttpServletRequest request, HttpServletResponse response) {
        return null; // return this.handshakeTemplate.handshake2(request, response);
    }

    /**
     * 捕捉异常。<br>
     * 接口调用抛出的异常在
     * <code>InvokeTemplate.processError()</code>中执行，其它情况的异常会走这里。
     *
     * @param request  请求
     * @param response 响应
     * @param e        异常
     */
    @ExceptionHandler(value = Throwable.class)
    public void jsonErrorHandler(HttpServletRequest request, HttpServletResponse response, Throwable e) {
        Logger.error("jsonErrorHandler error", e);
        ApiResult result = new ApiResult();
        result.setCode(Errors.SYS_ERROR.getCode());
        result.setMsg(e.getMessage());
        try {
            apiConfig.getWriter().write(request, response, result);
        } catch (Exception e1) {
            Logger.error("写json失败", e1);
        }
    }

    @Component
    private static class Ctx implements ApplicationContextAware {

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            ctx = applicationContext;
        }

    }

}
