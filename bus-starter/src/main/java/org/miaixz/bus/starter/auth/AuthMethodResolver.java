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
package org.miaixz.bus.starter.auth;

import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.core.lang.annotation.Authenticate;
import org.miaixz.bus.spring.ContextBuilder;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 授权方法参数解析器，用于将带有{@link Authenticate}注解的方法参数自动注入当前登录用户信息。
 *
 * <p>
 * 该类实现了Spring MVC的{@link HandlerMethodArgumentResolver}接口，用于在控制器方法调用前，
 * 自动将当前登录用户信息注入到带有{@link Authenticate}注解的{@link Authorize}类型参数中。
 * </p>
 *
 * <p>
 * 使用示例：
 * </p>
 * 
 * <pre>
 * &#64;RestController
 * &#64;RequestMapping("/user")
 * public class UserController {
 *
 *     &#64;GetMapping("/info")
 *     public UserInfo getUserInfo(&#64;Authenticate Authorize user) {
 *         // 直接使用user参数，无需手动获取用户信息
 *         return userService.getUserInfo(user.getId());
 *     }
 * }
 * </pre>
 *
 * <p>
 * 在上述示例中，当调用{@code /user/info}接口时，{@code AuthMethodResolver}会自动将当前登录用户信息 注入到{@code user}参数中，开发者无需手动从请求中获取用户信息。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AuthMethodResolver implements HandlerMethodArgumentResolver {

    /**
     * 判断方法参数是否支持解析。
     *
     * <p>
     * 当且仅当满足以下条件时，该方法返回true：
     * </p>
     * <ul>
     * <li>参数类型是{@link Authorize}类或其子类</li>
     * <li>参数带有{@link Authenticate}注解</li>
     * </ul>
     *
     * @param parameter 要检查的方法参数
     * @return 如果支持解析则返回true，否则返回false
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(Authorize.class)
                && parameter.hasParameterAnnotation(Authenticate.class);
    }

    /**
     * 解析方法参数，将当前登录用户信息注入到参数中。
     *
     * <p>
     * 该方法从Spring上下文中获取当前登录用户信息，并将其作为方法参数的值返回。
     * </p>
     *
     * @param parameter             要解析的方法参数
     * @param modelAndViewContainer ModelAndView容器，可用于设置模型和视图
     * @param nativeWebRequest      原生Web请求对象
     * @param webDataBinderFactory  Web数据绑定工厂，可用于创建WebDataBinder实例
     * @return 当前登录用户信息对象
     */
    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer modelAndViewContainer,
            NativeWebRequest nativeWebRequest,
            WebDataBinderFactory webDataBinderFactory) {
        return ContextBuilder.getAuthorize();
    }

}
