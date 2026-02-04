/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.annotation;

import java.io.Serializable;
import java.lang.annotation.*;

import org.miaixz.bus.core.lang.Normal;

/**
 * A general-purpose annotation used to assign a string-based name to a component or injection point. This is commonly
 * used in dependency injection frameworks to distinguish between multiple beans of the same type. It serves a similar
 * purpose to {@code jakarta.inject.Named}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Binding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
public @interface Named {

    /**
     * The name of the component or injection point.
     *
     * @return The assigned name.
     */
    String value() default Normal.EMPTY;

    /**
     * A concrete, serializable implementation of the {@link Named} annotation. This class allows for the programmatic
     * creation of {@code Named} annotation instances, which is useful for frameworks that operate on annotations at
     * runtime.
     */
    class Names implements Named, Serializable {

        /**
         * The name value of this annotation instance.
         */
        private final String value;

        /**
         * Creates a new instance of {@code Names}.
         *
         * @param value The name for this annotation. Must not be null.
         */
        public Names(String value) {
            this.value = checkNotNull(value, "name");
        }

        /**
         * A utility method to check for null references, similar to {@code java.util.Objects.requireNonNull}.
         *
         * @param reference    The reference to check.
         * @param errorMessage The error message to use if the reference is null.
         * @param <T>          The type of the reference.
         * @return The non-null reference.
         * @throws NullPointerException if the reference is null.
         */
        public static <T> T checkNotNull(T reference, Object errorMessage) {
            if (null == reference) {
                throw new NullPointerException(String.valueOf(errorMessage));
            } else {
                return reference;
            }
        }

        /**
         * Returns the name associated with this annotation instance.
         *
         * @return The name value.
         */
        @Override
        public String value() {
            return this.value;
        }

        /**
         * Computes the hash code for this annotation instance.
         *
         * @return The hash code.
         */
        @Override
        public int hashCode() {
            // This calculation is specified in the Annotation.hashCode() documentation.
            return (127 * "value".hashCode()) ^ value.hashCode();
        }

        /**
         * Compares this annotation instance to another object for equality.
         *
         * @param o The object to compare with.
         * @return {@code true} if the given object is an instance of {@code Named} and has the same value.
         */
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Named)) {
                return false;
            }

            Named other = (Named) o;
            return value.equals(other.value());
        }

        /**
         * Returns the annotation type of this instance.
         *
         * @return The {@code Named.class} type.
         */
        @Override
        public Class<? extends Annotation> annotationType() {
            return Named.class;
        }

    }

}
