/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
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
