/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.spring.validate;

import org.aoju.bus.spring.core.proxy.AspectjProxyChain;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

/**
 * AOP切面切点
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
@Aspect
@Order(99)
public class AspectjProxyPoint {

    /**
     * requestMapping
     * putMapping
     * postMapping
     * patchMapping
     * modelAttribute
     * getMapping
     * deleteMapping
     * CrossOrigin
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.PutMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.PostMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.PatchMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.ModelAttribute)" +
            "||@annotation(org.springframework.web.bind.annotation.GetMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.DeleteMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.CrossOrigin)" +
            "||execution(* *(@org.aoju.bus.validate.annotation.Valid (*), ..))")
    public void match() {

    }

    /**
     * 执行结果
     *
     * @param joinPoint 切点
     * @return 返回结果
     * @throws Throwable 异常
     */
    @Around("match()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        return new AutoValidateAdvice().access(new AspectjProxyChain(joinPoint));
    }

}
