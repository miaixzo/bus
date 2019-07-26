package org.aoju.bus.core.builder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on the fields to get the summary instead of the detailed
 * information when using {@link ReflectionToStringBuilder}.
 *
 * <p>
 * Notice that not all {@link ToStringStyle} implementations support the
 * appendSummary method.
 * </p>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ToStringSummary {

}
