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
package org.aoju.bus.spring.limiter;

import org.aoju.bus.limiter.ErrorHandler;
import org.aoju.bus.limiter.LimitedFallbackResolver;
import org.aoju.bus.limiter.execute.LimiterExecutionContext;
import org.aoju.bus.logger.Logger;
import org.springframework.context.annotation.Bean;

/**
 * @author Kimi Liu
 * @version 3.6.2
 * @since JDK 1.8
 */
public class DefaultConfiguration {

    @Bean
    ErrorHandler defaultErrorHandler() {
        ErrorHandler errorHandler = new ErrorHandler() {
            @Override
            public boolean resolve(Throwable throwable, LimiterExecutionContext executionContext) {
                Logger.info(throwable.getMessage());
                throw new RuntimeException(throwable.getMessage());
            }
        };
        return errorHandler;
    }

    @Bean
    LimitedFallbackResolver defaultFallbackResolver() {
        LimitedFallbackResolver limitedFallbackResolver
                = (method, clazz, args, limitedResource, target) -> {
            throw new RuntimeException("no message available");
        };
        return limitedFallbackResolver;
    }

}
