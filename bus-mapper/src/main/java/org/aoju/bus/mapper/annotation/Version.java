package org.aoju.bus.mapper.annotation;

import org.aoju.bus.mapper.version.DefaultNextVersion;
import org.aoju.bus.mapper.version.NextVersion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 版本信息
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Version {

    /**
     * 下一个版本号的算法，默认算法支持 Integer 和 Long，在原基础上 +1
     */
    Class<? extends NextVersion> nextVersion() default DefaultNextVersion.class;

}
