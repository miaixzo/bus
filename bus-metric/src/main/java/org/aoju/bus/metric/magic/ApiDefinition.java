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

import org.aoju.bus.metric.builtin.MethodCaller;

import java.lang.reflect.Method;

/**
 * 接口定义，负责存放定义的接口信息
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8++
 */
public class ApiDefinition implements ApiMeta {

    /**
     * 接口名
     */
    private String name;
    /**
     * 版本号
     */
    private String version;
    /**
     * 接口描述，如果定义了@ApiDocMethod注解会同步其description属性到这
     */
    private String description;
    /**
     * 模块名
     */
    private String moduleName;
    /**
     * 排序
     */
    private int orderIndex;

    /**
     * 接口对应的Service类
     */
    private Object handler;
    /**
     * 接口对应的方法
     */
    private Method method;
    /**
     * 方法参数的class
     */
    private Class<?> methodArguClass;

    private boolean ignoreSign;
    private boolean ignoreValidate;
    private boolean ignoreJWT;
    private boolean ignoreToken;
    private boolean wrapResult = Boolean.TRUE;

    private boolean singleParameter;

    private boolean noReturn;

    private MethodCaller methodCaller;

    public String getNameVersion() {
        return this.name + this.version;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public Object getHandler() {
        return handler;
    }

    public void setHandler(Object handler) {
        this.handler = handler;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    @Override
    public Class<?> getMethodArguClass() {
        return methodArguClass;
    }

    public void setMethodArguClass(Class<?> methodArguClass) {
        this.methodArguClass = methodArguClass;
    }

    @Override
    public boolean isIgnoreSign() {
        return ignoreSign;
    }

    public void setIgnoreSign(boolean ignoreSign) {
        this.ignoreSign = ignoreSign;
    }

    @Override
    public boolean isIgnoreValidate() {
        return ignoreValidate;
    }

    public void setIgnoreValidate(boolean ignoreValidate) {
        this.ignoreValidate = ignoreValidate;
    }

    @Override
    public boolean isWrapResult() {
        return wrapResult;
    }

    public void setWrapResult(boolean wrapResult) {
        this.wrapResult = wrapResult;
    }

    public boolean isSingleParameter() {
        return singleParameter;
    }

    public void setSingleParameter(boolean singleParameter) {
        this.singleParameter = singleParameter;
    }

    public boolean isNoReturn() {
        return noReturn;
    }

    public void setNoReturn(boolean noReturn) {
        this.noReturn = noReturn;
    }

    @Override
    public boolean noReturn() {
        return noReturn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public MethodCaller getMethodCaller() {
        return methodCaller;
    }

    public void setMethodCaller(MethodCaller methodCaller) {
        this.methodCaller = methodCaller;
    }

    @Override
    public boolean isIgnoreJWT() {
        return ignoreJWT;
    }

    public void setIgnoreJWT(boolean ignoreJWT) {
        this.ignoreJWT = ignoreJWT;
    }

    @Override
    public boolean isIgnoreToken() {
        return ignoreToken;
    }

    public void setIgnoreToken(boolean ignoreToken) {
        this.ignoreToken = ignoreToken;
    }

}
