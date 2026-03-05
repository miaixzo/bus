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
package org.miaixz.bus.spring.boot;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.Aware;

/**
 * Interface to be implemented by any object that wishes to be notified of the {@link StartupReporter} that it operates
 * in.
 * <p>
 * This is similar to Spring's {@link org.springframework.context.ApplicationContextAware} but specifically for the
 * {@link StartupReporter} instance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface StartupReporterAware extends Aware {

    /**
     * Set the {@link StartupReporter} that this object runs in. Normally this call will occur prior to any other
     * initialization callbacks being applied, e.g. {@code InitializingBean}'s {@code afterPropertiesSet()} or a custom
     * init-method.
     *
     * @param startupReporter The {@link StartupReporter} object to be used by this object.
     * @throws BeansException in case of initialization errors.
     */
    void setStartupReporter(StartupReporter startupReporter) throws BeansException;

}
