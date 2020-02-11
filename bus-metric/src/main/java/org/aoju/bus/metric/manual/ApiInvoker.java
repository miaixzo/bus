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
package org.aoju.bus.metric.manual;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.lang.exception.InvalidParamsException;
import org.aoju.bus.core.utils.BeanUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.metric.ApiContext;
import org.aoju.bus.metric.ApiHandler;
import org.aoju.bus.metric.ApiRegister;
import org.aoju.bus.metric.manual.docs.ApiDocHolder;
import org.aoju.bus.metric.manual.docs.ApiDocItem;
import org.aoju.bus.metric.register.SingleParameterContext;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理客户端请求分发
 *
 * @author Kimi Liu
 * @version 5.5.8
 * @since JDK 1.8++
 */
public class ApiInvoker implements Invoker {

    private static final ApiHandler[] EMPTY_INTERCEPTOR_ARRAY = {};
    private static final EmptyObject EMPTY_OBJECT = new EmptyObject();

    public static Object call(ApiMeta apiMeta, Object methodArgu) throws InvocationTargetException, IllegalAccessException {
        Object invokeResult;
        if (methodArgu == null) {
            invokeResult = apiMeta.getMethod().invoke(apiMeta.getHandler());
        } else {
            invokeResult = apiMeta.getMethod().invoke(apiMeta.getHandler(), methodArgu);
        }
        return invokeResult;
    }

    @Override
    public Object invoke(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        ApiContext.setRequest(request);
        ApiContext.setResponse(response);
        try {
            // 解析参数
            ApiParam param = ApiContext.getConfig().getParamParser().parse(request);
            ApiContext.setApiParam(param);
            return this.doInvoke(param, request, response);
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
            }
            if (e instanceof InvalidParamsException) {
                InvalidParamsException ex = (InvalidParamsException) e;
                Logger.warn("业务参数错误,code:{}, msg:{}", ex.getErrcode(), ex.getMessage());
            } else {
                Logger.error(e.getMessage(), e);
            }
            throw e;
        }
    }

    @Override
    public Object invokeMock(HttpServletRequest request, HttpServletResponse response) {
        ApiContext.setRequest(request);
        ApiContext.setResponse(response);
        // 解析参数
        ApiParam param = ApiContext.getConfig().getParamParser().parse(request);
        ApiContext.setApiParam(param);

        ApiDocItem apiDocItem = ApiDocHolder.getApiDocBuilder().getApiDocItem(param.fatchName(), param.fatchVersion());
        if (apiDocItem == null) {
            return EMPTY_OBJECT;
        }
        Object resultData = apiDocItem.fatchResultData();
        if (resultData == null) {
            resultData = EMPTY_OBJECT;
        }
        return resultData;
    }

    protected String getHeader(HttpServletRequest request, String key) {
        String value = request.getHeader(key);
        if (value == null) {
            return null;
        }
        if (ApiContext.isEncryptMode()) {
            value = ApiContext.decryptAES(value);
        } else if (ApiContext.hasUseNewSSL(request)) {
            value = ApiContext.decryptAESFromBase64String(value);
        }
        return value;
    }

    /**
     * 调用具体的业务方法，返回业务结果
     *
     * @param param    参数
     * @param request  请求
     * @param response 响应
     * @return 返回最终结果
     * @throws Throwable 异常
     */
    protected Object doInvoke(ApiParam param, HttpServletRequest request, HttpServletResponse response) throws Throwable {
        ApiDefinition apiDefinition = this.getApiDefinition(param);
        ApiContext.setApiMeta(apiDefinition);
        // 方法参数
        Object methodArgu = null;
        // 返回结果
        Object invokeResult = null;

        Validator validator = ApiContext.getConfig().getValidator();

        param.setIgnoreSign(apiDefinition.isIgnoreSign());
        param.setIgnoreValidate(apiDefinition.isIgnoreValidate());
        // 验证操作，这里有负责验证签名参数
        validator.validate(param);

        // 业务参数json格式
        String busiJsonData = ApiContext.getConfig().getDataDecoder().decode(param);

        // 业务参数Class
        Class<?> arguClass = apiDefinition.getMethodArguClass();

        boolean isSingleParameter = apiDefinition.isSingleParameter();
        Object singleParamProxy = null;

        int interceptorIndex = 0;
        try {
            // 将参数绑定到业务方法参数上，业务方法参数可以定义的类型：JSONObject,Map<String,Object>,String,业务参数类
            if (arguClass != null) {
                if (arguClass == JSONObject.class) {
                    methodArgu = JSON.parseObject(busiJsonData);
                } else if (arguClass == Map.class) {
                    methodArgu = new HashMap<>(JSON.parseObject(busiJsonData));
                } else if (isSingleParameter) {
                    SingleParameterContext.SingleParameterContextValue value = SingleParameterContext.get(apiDefinition.getMethod());
                    if (value != null) {
                        JSONObject jsonObj = JSON.parseObject(busiJsonData);
                        methodArgu = jsonObj.getObject(value.getParamName(), arguClass);
                        singleParamProxy = jsonObj.toJavaObject(value.getWrapClass());
                    }
                } else {
                    methodArgu = JSON.parseObject(busiJsonData, arguClass);
                }
                this.bindUploadFile(methodArgu);
            }
            // 拦截器
            ApiHandler[] interceptors = ApiContext.getConfig().getInterceptors();
            if (interceptors == null) {
                interceptors = EMPTY_INTERCEPTOR_ARRAY;
            }

            //1. 调用preHandle
            for (int i = 0; i < interceptors.length; i++) {
                ApiHandler interceptor = interceptors[i];
                if (interceptor.match(apiDefinition) && !interceptor.preHandle(request, response, apiDefinition.getHandler(), methodArgu)) {
                    //1.1、失败时触发afterCompletion的调用
                    triggerAfterCompletion(apiDefinition, interceptorIndex, request, response, methodArgu, null, null);
                    return null;
                }
                //1.2、记录当前预处理成功的索引
                interceptorIndex = i;
            }

            // 验证业务参数JSR-303
            this.validateBizArgu(validator, methodArgu, singleParamProxy);

            /* *** 调用业务方法,被@Api标记的方法 ***/
            MethodCaller methodCaller = apiDefinition.getMethodCaller();
            if (methodCaller != null) {
                invokeResult = methodCaller.call(new ApiInvocation(apiDefinition, methodArgu));
            } else {
                invokeResult = call(apiDefinition, methodArgu);
            }

            //3、调用postHandle,业务方法调用后处理（逆序）
            for (int i = interceptors.length - 1; i >= 0; i--) {
                ApiHandler interceptor = interceptors[i];
                if (interceptor.match(apiDefinition)) {
                    interceptor.postHandle(request, response, apiDefinition.getHandler(), methodArgu, invokeResult);
                }
            }

            if (invokeResult == null) {
                invokeResult = EMPTY_OBJECT;
            }

            // 最终返回的对象
            Object finalReturn = this.wrapResult(apiDefinition, invokeResult);

            //4、触发整个请求处理完毕回调方法afterCompletion
            triggerAfterCompletion(apiDefinition, interceptorIndex, request, response, methodArgu, finalReturn, null);

            return finalReturn;
        } catch (Exception e) {
            this.triggerAfterCompletion(apiDefinition, interceptorIndex, request, response, methodArgu, invokeResult, e);
            throw e;
        }
    }

    protected ApiDefinition getApiDefinition(ApiParam param) {
        ApiDefinition apiDefinition = DefinitionHolder.getByParam(param);
        if (apiDefinition == null) {
            throw Errors.NO_API.getException(param.fatchName(), param.fatchVersion());
        }
        return apiDefinition;
    }

    protected Object wrapResult(ApiDefinition apiDefinition, Object invokeResult) {
        // 最终返回的对象
        Object finalReturn = invokeResult;
        if (apiDefinition.noReturn()) {
            finalReturn = null;
        } else if (apiDefinition.isWrapResult()) {
            // 对返回结果包装
            finalReturn = ApiContext.getConfig().getResultCreator().createResult(invokeResult);
        }
        return finalReturn;
    }

    private void triggerAfterCompletion(ApiDefinition definition, int interceptorIndex,
                                        HttpServletRequest request, HttpServletResponse response, Object argu, Object result, Exception e) throws Exception {
        // 5、触发整个请求处理完毕回调方法afterCompletion （逆序从1.2中的预处理成功的索引处的拦截器执行）
        ApiHandler[] interceptors = ApiContext.getConfig().getInterceptors();

        if (interceptors != null && interceptors.length > 0) {
            for (int i = interceptorIndex; i >= 0; i--) {
                ApiHandler interceptor = interceptors[i];
                if (interceptor.match(definition)) {
                    interceptor.afterCompletion(request, response, definition.getHandler(), argu, result, e);
                }
            }
        }
    }

    /**
     * 校验业务参数
     *
     * @param validator        校验器
     * @param methodArgu       方法参数值
     * @param singleParamProxy 单值参数代理对象
     */
    protected void validateBizArgu(Validator validator, Object methodArgu, Object singleParamProxy) {
        if (singleParamProxy != null) {
            validator.validateBusiParam(singleParamProxy);
        } else {
            validator.validateBusiParam(methodArgu);
        }
    }

    /**
     * 绑定上传文件到参数类当中
     *
     * @param args 参数
     */
    protected void bindUploadFile(Object args) {
        if (args != null) {
            Upload upload = ApiContext.getUploadContext();
            if (upload != null) {
                List<MultipartFile> files = upload.getAllFile();
                if (files != null && files.size() > 0) {
                    Map<String, Object> filesMap = new HashMap<>(files.size());

                    for (MultipartFile file : files) {
                        filesMap.put(file.getName(), file);
                    }

                    BeanUtils.copyProperties(filesMap, args);

                    Field field = ApiRegister.getListFieldWithGeneric(args, MultipartFile.class);
                    if (field != null) {
                        ApiRegister.invokeFieldValue(args, field.getName(), files);
                    }
                }
            }
        }
    }

    private static class EmptyObject implements Serializable {
        private static final long serialVersionUID = 1L;
    }

}
