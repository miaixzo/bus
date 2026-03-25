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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.validate.Builder;
import org.miaixz.bus.validate.Context;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * An AOP advice class that automatically handles parameter validation for intercepted methods.
 * <p>
 * This advice intercepts method executions, extracts the arguments and their corresponding validation annotations, and
 * then uses the {@code bus-validate} framework to perform the validation checks before the actual method logic is
 * executed. It is intended to be used with an {@code @Around} advice.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AutoValidateAdvice {

    /**
     * The core advice method that performs automatic parameter validation.
     * <p>
     * It intercepts the method call, resolves the target method (handling interfaces), discovers parameter names and
     * annotations, and then iterates through each argument to apply validation rules using
     * {@link Builder#on(Object, Annotation[], Context, String)}. After validation, it proceeds with the original method
     * execution.
     *
     * @param joinPoint The AOP join point, expected to be a {@link ProceedingJoinPoint} for {@code @Around} advice.
     * @return The result of the original method's execution.
     * @throws Throwable if an exception occurs during validation or method execution.
     */
    public Object access(JoinPoint joinPoint) throws Throwable {
        // Get the method and its arguments
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] arguments = joinPoint.getArgs();

        // If the method is from an interface, try to get the implementation method
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = joinPoint.getTarget().getClass()
                        .getDeclaredMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                Logger.info(
                        "Cannot find the specified method in the implementation class, skipping validation for method: {}",
                        method.getName());
                return proceed(joinPoint, arguments); // Skip validation and proceed
            }
        }

        // Get parameter annotations and names
        Annotation[][] annotations = method.getParameterAnnotations();
        String[] names = new DefaultParameterNameDiscoverer().getParameterNames(method);
        if (names == null || names.length == 0) {
            names = new String[arguments.length]; // Assign default names if discovery fails
            for (int i = 0; i < names.length; i++) {
                names[i] = Normal.EMPTY + i;
            }
        }

        // Perform validation for each argument
        for (int i = 0; i < arguments.length; i++) {
            Builder.on(arguments[i], annotations[i], Context.newInstance(), names[i]);
        }

        // Continue with the original method execution
        return proceed(joinPoint, arguments);
    }

    /**
     * Proceeds with the execution of the intercepted method.
     * <p>
     * This helper method ensures that the AOP chain continues. It is designed to work with {@link ProceedingJoinPoint},
     * which is available in {@code @Around} advice.
     *
     * @param joinPoint The AOP join point.
     * @param arguments The method arguments.
     * @return The result of the original method execution.
     * @throws Throwable if an exception is thrown by the intercepted method.
     */
    private Object proceed(JoinPoint joinPoint, Object[] arguments) throws Throwable {
        // If it's a ProceedingJoinPoint (used in @Around advice), call proceed.
        if (joinPoint instanceof ProceedingJoinPoint) {
            return ((ProceedingJoinPoint) joinPoint).proceed(arguments);
        }
        // This block should ideally not be reached if the advice is correctly configured as @Around.
        Logger.warn(
                "AutoValidateAdvice used with a non-proceeding join point type: {}. The original method cannot be executed by this advice.",
                joinPoint.getKind());
        return null;
    }

}
