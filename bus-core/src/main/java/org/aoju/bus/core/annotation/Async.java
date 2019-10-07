package org.aoju.bus.core.annotation;

import org.aoju.bus.core.utils.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a class or method to as "asynchronous"
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Async {

    /**
     * When a `public void` method has been annotated as `@Async`, ActFramework will
     * enhance the class by adding an new method as a copy of the original method, the name
     * of the new method will be the result of calling the original method.
     */
    class MethodNameTransformer {

        /**
         * Returns the new async method's name based on the give name
         *
         * @param methodName the original method name
         * @return the name of the async method that pairs to the original method
         */
        public static String transform(String methodName) {
            return StringUtils.concat(true, new String[]{"__act_", methodName, "_async"});
        }
    }

}