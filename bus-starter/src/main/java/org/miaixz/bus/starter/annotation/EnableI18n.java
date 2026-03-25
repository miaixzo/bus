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
package org.miaixz.bus.starter.annotation;

import org.miaixz.bus.starter.i18n.I18nConfiguration;
import org.miaixz.bus.starter.i18n.I18nMessage;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enables internationalization (i18n) support.
 * <p>
 * This annotation, when placed on a Spring {@code @Configuration} class, imports the {@link I18nConfiguration} and the
 * {@link I18nMessage} helper bean. This sets up the necessary {@link org.springframework.context.MessageSource} and
 * provides a convenient way to access localized messages.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Inherited
@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Import({ I18nConfiguration.class, I18nMessage.class })
public @interface EnableI18n {

}
