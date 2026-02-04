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
package org.miaixz.bus.spring.metrics;

/**
 * Interface for customizing {@link BeanMetrics}.
 * <p>
 * Implementations of this interface can be used to perform custom processing on bean metrics, such as adding additional
 * attributes or modifying existing metrics. This allows for tailored monitoring needs during the bean initialization
 * process.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface BeanMetricsCustomizer {

    /**
     * Customizes the startup metrics of a bean.
     * <p>
     * This method is invoked during the bean initialization process, allowing for custom processing of the bean's
     * metrics. Additional attributes can be added or existing metrics can be modified to meet specific monitoring
     * requirements.
     * </p>
     *
     * @param beanName The name of the bean.
     * @param bean     The bean instance.
     * @param beanStat The {@link BeanMetrics} object representing the bean's statistics.
     * @return The customized {@link BeanMetrics} object. If {@code null} is returned, subsequent
     *         {@code BeanMetricsCustomizer}s will not be called for this bean.
     */
    BeanMetrics customize(String beanName, Object bean, BeanMetrics beanStat);

}
