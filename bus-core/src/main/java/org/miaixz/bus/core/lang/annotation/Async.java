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
package org.miaixz.bus.core.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Marks a method or all public methods in a class to be executed asynchronously. When a method is annotated with
 * {@code @Async}, the framework will typically execute it in a separate thread, allowing the caller to proceed without
 * waiting for the method to complete.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Async {

    /**
     * An internal utility class used by the framework to generate a unique name for the asynchronous method that wraps
     * the original annotated method. When a {@code public void} method is annotated with {@code @Async}, the framework
     * enhances the class by adding a new method that invokes the original. This class provides the naming convention
     * for that new method.
     */
    class MethodNameTransformer {

        /**
         * Constructs a new MethodNameTransformer.
         */
        private MethodNameTransformer() {
        }

        /**
         * Transforms the name of the original method into a new name for its asynchronous counterpart.
         *
         * @param methodName The name of the original method.
         * @return The name of the asynchronous method that will be paired with the original.
         */
        public static String transform(String methodName) {
            return StringKit.concat(true, new String[] { "__act_", methodName, "_async" });
        }
    }

}
