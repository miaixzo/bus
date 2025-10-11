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
