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
package org.miaixz.bus.spring.http;

import jakarta.servlet.http.HttpServletRequest;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.UrlKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.stream.Collectors;

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
public class CompositeArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 判断是否支持解析当前参数
     *
     * @param parameter 方法参数信息
     * @return true 如果参数不是简单类型（基本类型、String、Number等），false 否则
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ModelAttribute.class) || !isSimpleType(parameter.getParameterType());
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
    public Object resolveArgument(
            MethodParameter methodParameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new IllegalStateException("No HttpServletRequest available");
        }

        byte[] body;
        String contentType = request.getContentType();
        // 如果是 MutableRequestWrapper，优先使用其缓存的 body
        if (request instanceof MutableRequestWrapper wrapper) {
            request = wrapper.getRequest();
            body = wrapper.getBody();
            contentType = wrapper.getContentType();
        } else {
            // 读取输入流
            body = IoKit.readBytes(request.getInputStream());
            if (body == null || body.length == 0) {
                // 如果输入流为空且有参数，使用parameterMap
                if (MapKit.isNotEmpty(request.getParameterMap())) {
                    String paramString = request.getParameterMap().entrySet().stream()
                            .map(entry -> entry.getKey() + Symbol.EQUAL + String.join(Symbol.COMMA, entry.getValue()))
                            .collect(Collectors.joining(Symbol.AND));
                    body = paramString.getBytes(Charset.UTF_8);
                    contentType = MediaType.APPLICATION_FORM_URLENCODED;
                } else {
                    body = new byte[0];
                }
            }
        }

        // 创建目标对象实例
        Class<?> parameterType = methodParameter.getParameterType();
        Object target = parameterType.getDeclaredConstructor().newInstance();
        WebDataBinder binder = binderFactory.createBinder(webRequest, target, methodParameter.getParameterName());

        // 添加 query 参数
        MutablePropertyValues mpvs = new MutablePropertyValues(request.getParameterMap());

        // 处理 application/x-www-form-urlencoded 请求
        if (contentType != null && contentType.startsWith(MediaType.APPLICATION_FORM_URLENCODED)) {
            String bodyString = new String(body, Charset.UTF_8);
            if (!StringKit.isEmpty(bodyString)) {
                mpvs.addPropertyValues(UrlKit.decodeQuery(bodyString, Charset.UTF_8));
            }
        }

        // 处理 JSON 请求
        if (contentType != null && contentType.startsWith(MediaType.APPLICATION_JSON)) {
            String bodyString = new String(body, Charset.UTF_8);
            if (!bodyString.isEmpty() && JsonKit.isJson(bodyString)) {
                return JsonKit.toPojo(bodyString, parameterType);
            }
        }

        // 处理 multipart/form-data 请求（文件上传）
        if (request instanceof MultipartHttpServletRequest multipartRequest) {
            multipartRequest.getMultiFileMap().forEach((key, files) -> {
                if (files.size() == 1) {
                    mpvs.add(key, files.get(0)); // 单文件绑定到 MultipartFile
                } else {
                    mpvs.add(key, files); // 多文件绑定到 List<MultipartFile> 或 MultipartFile[]
                }
            });
        }

        if (mpvs.isEmpty() && !methodParameter.hasParameterAnnotation(ModelAttribute.class)) {
            throw new IllegalArgumentException("No parameters provided for " + methodParameter.getParameterName());
        }

        binder.bind(mpvs);
        if (binder.getBindingResult().hasErrors()) {
            throw new IllegalArgumentException("Binding errors: " + binder.getBindingResult().getAllErrors());
        }
        return target;
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
