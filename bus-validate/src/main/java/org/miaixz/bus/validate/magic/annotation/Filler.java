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
package org.miaixz.bus.validate.magic.annotation;

import java.lang.annotation.*;

/**
 * A functional annotation for interpolating values into validation error messages.
 * <p>
 * Within the framework, two default placeholder variables are provided for interpolation:
 * <ul>
 * <li>{@code ${field}}: The name of the field being validated.</li>
 * <li>{@code ${value}}: The string representation of the object being validated.</li>
 * </ul>
 * By marking a method within a custom validation annotation with {@code @Filler}, the method's return value can be used
 * as a custom placeholder in the {@code errmsg} of that annotation. The name of the placeholder is specified by the
 * {@link #value()} attribute of this annotation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Filler {

    /**
     * The name of the placeholder to be used in the error message template.
     *
     * @return the placeholder name.
     */
    String value();

}
