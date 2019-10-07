package org.aoju.bus.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to provide label for a field/getter when output to CLI table or Excel/CSV etc.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Label {
    /**
     * Specify the label
     *
     * @return the label of the field or getter method
     */
    String value();
}