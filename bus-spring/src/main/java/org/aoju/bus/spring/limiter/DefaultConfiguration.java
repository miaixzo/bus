package org.aoju.bus.spring.limiter;

import org.aoju.bus.limiter.ErrorHandler;
import org.aoju.bus.limiter.LimitedFallbackResolver;
import org.aoju.bus.limiter.execute.LimiterExecutionContext;
import org.aoju.bus.logger.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
