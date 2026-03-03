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
package org.miaixz.bus.starter.validate;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

/**
 * An AOP aspect that provides a proxy for automatic validation of controller method parameters.
 * <p>
 * This class uses Spring AOP to validate parameters annotated with validation constraints before the controller method
 * is executed. If validation fails, an exception is thrown, preventing the method from being called.
 *
 * <p>
 * This aspect matches methods of the following types:
 * </p>
 * <ul>
 * <li>Methods annotated with Spring Web annotations: {@code @RequestMapping}, {@code @GetMapping},
 * {@code @PostMapping}, etc.</li>
 * <li>Methods annotated with {@code @CrossOrigin}.</li>
 * <li>Methods with parameters annotated with {@code @Valid}.</li>
 * </ul>
 *
 * <p>
 * <strong>Usage Example:</strong>
 * 
 * <pre>{@code
 * &#64;RestController
 * &#64;RequestMapping("/user")
 * public class UserController {
 *
 *     &#64;PostMapping("/register")
 *     public UserDTO register(&#64;Valid UserRegisterDTO userDTO) {
 *         // If userDTO validation fails, this method will not be executed; an exception will be thrown directly.
 *         return userService.register(userDTO);
 *     }
 * }
 * }</pre>
 *
 * <p>
 * In the example above, when the {@code /user/register} endpoint is called, {@code AspectjValidateProxy} automatically
 * validates the {@code userDTO} parameter. If validation fails, an exception is thrown, and the method body is not
 * executed.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(99)
@Aspect
public class AspectjValidateProxy {

    /**
     * Defines the pointcut that matches all methods annotated with Spring Web annotations or methods with parameters
     * annotated with {@code @Valid}.
     * <p>
     * The pointcut expression matches methods with the following annotations:
     *
     * <ul>
     * <li>{@code @RequestMapping}</li>
     * <li>{@code @PutMapping}</li>
     * <li>{@code @PostMapping}</li>
     * <li>{@code @PatchMapping}</li>
     * <li>{@code @ModelAttribute}</li>
     * <li>{@code @GetMapping}</li>
     * <li>{@code @DeleteMapping}</li>
     * <li>{@code @CrossOrigin}</li>
     * <li>Methods with a parameter annotated with {@code @org.miaixz.bus.validate.magic.annotation.Valid}</li>
     * </ul>
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)"
            + "||@annotation(org.springframework.web.bind.annotation.PutMapping)"
            + "||@annotation(org.springframework.web.bind.annotation.PostMapping)"
            + "||@annotation(org.springframework.web.bind.annotation.PatchMapping)"
            + "||@annotation(org.springframework.web.bind.annotation.ModelAttribute)"
            + "||@annotation(org.springframework.web.bind.annotation.GetMapping)"
            + "||@annotation(org.springframework.web.bind.annotation.DeleteMapping)"
            + "||@annotation(org.springframework.web.bind.annotation.CrossOrigin)"
            + "||execution(* *(@org.miaixz.bus.validate.magic.annotation.Valid (*), ..))")
    public void match() {
        // This method is empty as it's just for defining the pointcut.
    }

    /**
     * Around advice that applies validation logic before and after the matched method execution.
     * <p>
     * This method invokes the {@link AutoValidateAdvice} to perform parameter validation before the target method is
     * executed. If validation passes, the target method proceeds; otherwise, an exception is thrown.
     * </p>
     *
     * @param point The join point, which contains information about the target method.
     * @return The result of the target method's execution.
     * @throws Throwable if validation fails or the target method throws an exception.
     */
    @Around("match()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        return new AutoValidateAdvice().access(point);
    }

}
