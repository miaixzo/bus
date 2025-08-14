/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring.web;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 自动参数解析器
 * <p>
 * 统一处理多种请求格式的参数绑定：
 * <ul>
 * <li>JSON请求 (application/json)</li>
 * <li>表单数据 (application/x-www-form-urlencoded)</li>
 * <li>文件上传 (multipart/form-data)</li>
 * </ul>
 * 通过反射自动将请求参数绑定到控制器方法参数对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AutoArgumentResolver implements HandlerMethodArgumentResolver {

    private List<JsonConverterConfigurer> converters;

    /**
     * 构造方法
     * <p>
     * 初始化 ObjectMapper 实例： 1. 尝试从 Spring 容器获取已配置的 ObjectMapper 2. 如果未找到，则创建默认 ObjectMapper 实例
     */
    public AutoArgumentResolver(List<JsonConverterConfigurer> converters) {
        this.converters = converters;
        if (this.converters == null && this.converters.isEmpty()) {
            throw new InternalException("No ObjectMapper bean found, using default ObjectMapper");
        }
    }

    /**
     * 判断是否支持解析当前参数
     *
     * @param parameter 方法参数信息
     * @return true 如果参数不是简单类型（基本类型、String、Number等），false 否则
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return !isSimpleType(parameter.getParameterType());
    }

    /**
     * 解析请求参数并绑定到目标对象
     *
     * @param methodParameter 方法参数信息
     * @param mavContainer    模型和视图容器
     * @param webRequest      原生 Web 请求
     * @param binderFactory   数据绑定工厂
     * @return 绑定好参数的目标对象
     * @throws Exception 解析过程中可能抛出的异常
     */
    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        // 获取原生 HttpServletRequest 对象
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new IllegalStateException("No HttpServletRequest available");
        }

        Class<?> parameterType = methodParameter.getParameterType();
        String contentType = request.getContentType();

        // 处理 JSON 请求
        if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return parseJsonRequest(request, parameterType);
        }

        // 创建目标对象实例
        Object target = parameterType.getDeclaredConstructor().newInstance();
        // 创建数据绑定器
        WebDataBinder binder = binderFactory.createBinder(webRequest, target, methodParameter.getParameterName());

        // 处理 application/x-www-form-urlencoded 或默认情况
        MutablePropertyValues mpvs = new MutablePropertyValues(request.getParameterMap());

        // 处理 multipart/form-data 文件上传
        if (contentType != null && contentType.contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            // 转换为 MultipartHttpServletRequest 以支持文件上传
            MultipartHttpServletRequest multipartRequest = request instanceof MultipartHttpServletRequest
                    ? (MultipartHttpServletRequest) request
                    : new StandardMultipartHttpServletRequest(request);

            // 获取所有表单字段（包括文件字段）
            mpvs = new MutablePropertyValues(multipartRequest.getParameterMap());

            // 处理文件字段绑定
            bindMultipartFiles(parameterType, target, multipartRequest.getFileMap());
        }

        // 执行数据绑定
        binder.bind(mpvs);
        return target;
    }

    /**
     * 解析 JSON 请求
     *
     * @param request       HTTP 请求对象
     * @param parameterType 目标参数类型
     * @return 解析后的对象
     */
    private Object parseJsonRequest(HttpServletRequest request, Class<?> parameterType) {
        try (InputStream inputStream = request.getInputStream()) {
            JsonConverterConfigurer configurer = this.converters.getFirst();
            if (configurer instanceof JacksonConverterConfigurer) {
                // 使用 Jackson 解析 JSON 请求体到目标对象
                return new ObjectMapper().readValue(inputStream, parameterType);
            } else if (configurer instanceof GsonConverterConfigurer) {
                // 使用 Gson 解析 JSON 请求体到目标对象
                return new GsonBuilder().create().fromJson(new String(inputStream.readAllBytes(), Charset.UTF_8),
                        parameterType);
            } else if (configurer instanceof FastJsonConverterConfigurer) {
                // 使用 Fastjson 解析 JSON 请求体到目标对象
                return JSON.parseObject(inputStream, parameterType);
            } else {
                throw new IllegalStateException("Unsupported JSON parser type: " + parameterType);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse JSON body", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JSON body with " + parameterType, e);
        }
    }

    /**
     * 绑定 MultipartFile 文件到目标对象的对应字段
     *
     * @param parameterType 目标对象类型
     * @param target        目标对象实例
     * @param fileMap       文件映射表（字段名 -> MultipartFile）
     * @throws IllegalAccessException 字段访问异常
     */
    private void bindMultipartFiles(Class<?> parameterType, Object target, Map<String, MultipartFile> fileMap)
            throws IllegalAccessException {
        if (!fileMap.isEmpty()) {
            // 遍历目标对象的所有字段
            for (Field field : parameterType.getDeclaredFields()) {
                // 检查字段类型是否为 MultipartFile
                if (MultipartFile.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true); // 设置可访问私有字段
                    MultipartFile file = fileMap.get(field.getName());
                    if (file != null) {
                        // 将文件对象设置到目标字段
                        field.set(target, file);
                    }
                }
            }
        }
    }

    /**
     * 判断是否为简单类型
     * <p>
     * 简单类型包括：
     * <ul>
     * <li>基本类型</li>
     * <li>String</li>
     * <li>Number</li>
     * <li>Boolean</li>
     * <li>Character</li>
     * <li>MultipartFile</li>
     * </ul>
     *
     * @param type 要检查的类型
     * @return true 如果是简单类型，false 否则
     */
    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() || type == String.class || Number.class.isAssignableFrom(type)
                || Boolean.class.isAssignableFrom(type) || Character.class.isAssignableFrom(type)
                || MultipartFile.class.isAssignableFrom(type);
    }

}