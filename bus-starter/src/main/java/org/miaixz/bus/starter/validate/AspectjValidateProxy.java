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
 * AOP验证代理切面类，用于对控制器方法参数进行自动验证。
 *
 * <p>
 * 该类使用Spring AOP技术，在控制器方法执行前对带有验证注解的参数进行验证， 如果验证失败则抛出异常，阻止方法继续执行。
 * </p>
 *
 * <p>
 * 该切面匹配以下类型的方法：
 * </p>
 * <ul>
 * <li>带有Spring Web注解的方法：@RequestMapping、@GetMapping、@PostMapping等</li>
 * <li>带有@CrossOrigin注解的方法</li>
 * <li>带有@Valid注解参数的方法</li>
 * </ul>
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
 *     &#64;PostMapping("/register")
 *     public UserDTO register(&#64;Valid UserRegisterDTO userDTO) {
 *         // 如果userDTO验证失败，方法不会执行，直接抛出异常
 *         return userService.register(userDTO);
 *     }
 * }
 * </pre>
 *
 * <p>
 * 在上述示例中，当调用{@code /user/register}接口时，{@code AspectjValidateProxy}会自动对 {@code userDTO}参数进行验证，如果验证失败则抛出异常，方法不会执行。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(99)
@Aspect
public class AspectjValidateProxy {

    /**
     * 定义切点，匹配所有带有Spring Web注解的方法或带有@Valid注解参数的方法。
     *
     * <p>
     * 切点表达式匹配以下注解或方法：
     * </p>
     * <ul>
     * <li>@RequestMapping</li>
     * <li>@PutMapping</li>
     * <li>@PostMapping</li>
     * <li>@PatchMapping</li>
     * <li>@ModelAttribute</li>
     * <li>@GetMapping</li>
     * <li>@DeleteMapping</li>
     * <li>@CrossOrigin</li>
     * <li>带有@Valid注解参数的方法</li>
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
        // 空方法体，仅用于定义切点
    }

    /**
     * 环绕通知，在匹配的方法执行前后进行验证处理。
     *
     * <p>
     * 该方法在目标方法执行前，调用{@link AutoValidateAdvice}对方法参数进行验证， 验证通过后继续执行目标方法，验证失败则抛出异常。
     * </p>
     *
     * @param point 切点，包含目标方法的信息
     * @return 目标方法的执行结果
     * @throws Throwable 如果验证失败或目标方法执行抛出异常
     */
    @Around("match()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        return new AutoValidateAdvice().access(point);
    }

}