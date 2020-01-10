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
package org.aoju.bus.metric;

import org.aoju.bus.logger.Logger;
import org.aoju.bus.metric.builtin.Errors;
import org.aoju.bus.metric.consts.MetricConsts;
import org.aoju.bus.metric.consts.RequestMode;
import org.aoju.bus.metric.magic.ApiMeta;
import org.aoju.bus.metric.magic.ApiParam;
import org.aoju.bus.metric.magic.Safety;
import org.aoju.bus.metric.magic.Upload;
import org.aoju.bus.metric.oauth2.Oauth2Manager;
import org.aoju.bus.metric.oauth2.OpenUser;
import org.aoju.bus.metric.session.SessionManager;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Locale;

/**
 * 应用上下文,方便获取信息
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8++
 */
public class Context {

    private static final String CLASS_NAME = Context.class.getName();

    private static final String NEW_SSL_KEY = CLASS_NAME + "new_ssl_key";
    private static final String ATTR_PARAM = CLASS_NAME + "param";
    private static final String ATTR_API_META = CLASS_NAME + "apimeta";
    private static final String ATTR_JWT_DATA = CLASS_NAME + "jwtdata";
    private static final String ATTR_REQUEST_MODE = CLASS_NAME + "requestmode";
    private static final String ATTR_UPLOAD_CONTEXT = CLASS_NAME + "uploadcontext";

    private static ThreadLocal<HttpServletRequest> request = new InheritableThreadLocal<>();
    private static ThreadLocal<HttpServletResponse> response = new InheritableThreadLocal<>();

    private static ApplicationContext applicationContext;
    private static ServletContext servletContext;

    private static Config config = new Config();

    private Context() {
    }

    public static String getName() {
        return config.getName();
    }

    private static void setAttr(String name, Object val) {
        HttpServletRequest request = getRequest();
        if (request != null) {
            request.setAttribute(name, val);
        }
    }

    private static Object getAttr(String name) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getAttribute(name);
    }

    /**
     * 获取随机码
     *
     * @return 返回随机码
     */
    public static String getRandomKey() {
        HttpSession session = getSession();
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute(MetricConsts.RANDOM_KEY_NAME);
    }

    public static void setRequestMode(RequestMode mode) {
        setAttr(ATTR_REQUEST_MODE, mode);
    }

    /**
     * AES解密
     *
     * @param value 待解密的值
     * @return 返回原文
     */
    public static String decryptAES(String value) {
        String randomKey = getRandomKey();
        try {
            Safety safety = config.getSafety();
            return safety.aesDecryptFromHex(value, randomKey);
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
            throw Errors.ERROR_SSL.getException();
        }
    }

    /**
     * AES解密
     *
     * @param value 待解密的值
     * @return 返回原文
     */
    public static String decryptAESFromBase64String(String value) {
        if (value == null) {
            Logger.error("aes value is null");
            throw Errors.ERROR_SSL.getException();
        }
        String randomKey = getRandomKey();
        try {
            if (randomKey == null) {
                throw new NullPointerException("randomKey is null");
            }
            Safety safety = config.getSafety();
            return safety.aesDecryptFromBase64String(value, randomKey);
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
            throw Errors.ERROR_SSL.getException();
        }
    }

    /**
     * 是否加密模式
     *
     * @return true，是加密模式
     */
    public static boolean isEncryptMode() {
        RequestMode mode = (RequestMode) getAttr(ATTR_REQUEST_MODE);
        if (mode == null) {
            return false;
        }
        return RequestMode.ENCRYPT == mode;
    }

    /**
     * 获取accessToken对应的用户
     *
     * @return 没有返回null
     */
    public static OpenUser getAccessTokenUser() {
        String accessToken = getSessionId();
        if (StringUtils.isEmpty(accessToken)) {
            return null;
        }
        Oauth2Manager manager = config.getOauth2Manager();
        return manager.getUserByAccessToken(accessToken);
    }


    /**
     * 获取HttpServletRequest
     *
     * @return HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        HttpServletRequest req = request.get();
        if (req == null) {
            RequestAttributes atri = RequestContextHolder.getRequestAttributes();
            if (atri != null) {
                req = ((ServletRequestAttributes) atri).getRequest();
            }
        }
        return req;
    }

    /**
     * 设置request，保存在ThreadLocal中
     *
     * @param req 网络请求
     */
    public static void setRequest(HttpServletRequest req) {
        request.set(req);
    }

    /**
     * 返回默认的HttpServletRequest.getSession();
     *
     * @return 没有返回null
     */
    public static HttpSession getSession() {
        HttpServletRequest req = getRequest();
        if (req == null) {
            return null;
        } else {
            return req.getSession();
        }
    }

    /**
     * 获取session管理器
     *
     * @return 返回SessionManager
     */
    public static SessionManager getSessionManager() {
        return config.getSessionManager();
    }

    /**
     * 返回自定义的session,被SessionManager管理
     *
     * @return 如果sessionId为null，则返回null
     */
    public static HttpSession getManagedSession() {
        String sessionId = getSessionId();
        if (sessionId != null) {
            return getSessionManager().getSession(sessionId);
        } else {
            return null;
        }
    }

    /**
     * 同getSessionId()
     *
     * @return 返回accessToken, 没有返回null
     */
    public static String getAccessToken() {
        return getSessionId();
    }

    /**
     * 获取登陆的token
     *
     * @return 没有返回null
     */
    public static String getSessionId() {
        ApiParam apiParam = getApiParam();
        if (apiParam == null) {
            return null;
        }
        return apiParam.fatchAccessToken();
    }

    /**
     * 获取本地化，从HttpServletRequest中获取，没有则返回Locale.SIMPLIFIED_CHINESE
     *
     * @return Locale
     */
    public static Locale getLocal() {
        HttpServletRequest req = getRequest();
        if (req == null) {
            return Locale.SIMPLIFIED_CHINESE;
        }
        return req.getLocale();
    }

    /**
     * 获取系统参数
     *
     * @return 返回ApiParam
     */
    public static ApiParam getApiParam() {
        return (ApiParam) getAttr(ATTR_PARAM);
    }

    public static void setApiParam(ApiParam apiParam) {
        setAttr(ATTR_PARAM, apiParam);
    }

    public static Config getConfig() {
        return config;
    }

    public static void setConfig(Config config) {
        Context.config = config;
    }

    /**
     * 获取spring应用上下文
     *
     * @return 返回spring应用上下文
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        Context.applicationContext = applicationContext;
        if (applicationContext instanceof ConfigurableWebApplicationContext) {
            servletContext = ((ConfigurableWebApplicationContext) applicationContext).getServletContext();
        }
    }

    public static ServletContext getServletContext() {
        if (servletContext != null) {
            return servletContext;
        } else {
            ServletContext ctx = null;
            HttpSession session = getSession();
            if (session != null) {
                ctx = session.getServletContext();
            }
            return ctx;
        }
    }

    /**
     * 获取上传文件，如果客户端有文件上传，从这里取。
     *
     * @return 如果没有文件上传，返回null
     */
    public static Upload getUploadContext() {
        return (Upload) getAttr(ATTR_UPLOAD_CONTEXT);
    }

    public static void setUploadContext(Upload uploadCtx) {
        setAttr(ATTR_UPLOAD_CONTEXT, uploadCtx);
    }

    public static void useNewSSL(HttpServletRequest request) {
        request.setAttribute(NEW_SSL_KEY, true);
    }

    public static boolean hasUseNewSSL(HttpServletRequest request) {
        return request.getAttribute(NEW_SSL_KEY) != null;
    }

    /**
     * <strong>！！！禁止调用此方法！！！！do NOT use this method!</strong>
     * 清除数据，防止内存泄露。
     */
    public static void clean() {
        request.remove();
        response.remove();
    }

    /**
     * 获取response
     *
     * @return 返回response
     */
    public static HttpServletResponse getResponse() {
        return response.get();
    }

    /**
     * 设置response
     *
     * @param resp response
     */
    public static void setResponse(HttpServletResponse resp) {
        response.set(resp);
    }

    /**
     * 获取ApiMeta
     *
     * @return 返回ApiMeta
     */
    public static ApiMeta getApiMeta() {
        return (ApiMeta) getAttr(ATTR_API_META);
    }

    public static void setApiMeta(ApiMeta apiMeta) {
        setAttr(ATTR_API_META, apiMeta);
    }

}
