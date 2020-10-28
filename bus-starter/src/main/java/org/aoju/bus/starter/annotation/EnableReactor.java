package org.aoju.bus.starter.annotation;

import org.aoju.bus.starter.goalie.ReactorConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * reactive endpoint
 *
 * @author Justubborn
 * @since 2020/10/27
 */
@Inherited
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({ReactorConfiguration.class})
public @interface EnableReactor {
}
