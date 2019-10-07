package org.aoju.bus.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the sort order for an annotated component
 * <p>
 * When sorting a list of element with (potentially) the
 * `@Order` annotation, the one without the
 * annotation is put in the head of the list, followed
 * by the one with less order weight value and then
 * followed by the one with greater order weight value
 *
 * @see Sorted
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Order {

    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    /**
     * Specify the order weight.
     */
    int value();

}