/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * Argument resolver for authorization, used to automatically inject the current logged-in user's information into
 * method parameters annotated with {@link Authenticate}.
 * <p>
 * This class implements Spring MVC's {@link HandlerMethodArgumentResolver} interface to automatically inject the
 * current user's information into parameters of type {@link Authorize} that are annotated with {@link Authenticate}
 * before a controller method is invoked.
 * </p>
 *
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 * 
 * <pre>{@code
 * &#64;RestController
 * &#64;RequestMapping("/user")
 * public class UserController {
 *
 *     &#64;GetMapping("/info")
 *     public UserInfo getUserInfo(&#64;Authenticate Authorize user) {
 *         // The 'user' parameter is directly available without manual retrieval.
 *         return userService.getUserInfo(user.getId());
 *     }
 * }
 * }</pre>
 * <p>
 * In the example above, when the {@code /user/info} endpoint is called, {@code AuthMethodResolver} automatically
 * injects the current logged-in user's information into the {@code user} parameter, so the developer does not need to
 * manually retrieve it from the request.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AuthMethodResolver implements HandlerMethodArgumentResolver {

    /**
     * Determines if this resolver supports the given method parameter.
     * <p>
     * This method returns {@code true} if and only if the following conditions are met:
     * </p>
     * <ul>
     * <li>The parameter type is assignable from {@link Authorize}.</li>
     * <li>The parameter is annotated with {@link Authenticate}.</li>
     * </ul>
     *
     * @param parameter The method parameter to check.
     * @return {@code true} if this resolver supports the parameter, {@code false} otherwise.
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(Authorize.class)
                && parameter.hasParameterAnnotation(Authenticate.class);
    }

    /**
     * Resolves the method argument by injecting the current logged-in user's information.
     * <p>
     * This method retrieves the current user's information from the Spring context and returns it as the value for the
     * method parameter.
     * </p>
     *
     * @param parameter             The method parameter to resolve.
     * @param modelAndViewContainer The ModelAndView container, which can be used to set the model and view.
     * @param nativeWebRequest      The native web request object.
     * @param webDataBinderFactory  A factory for creating WebDataBinder instances.
     * @return The current logged-in user's information object.
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
