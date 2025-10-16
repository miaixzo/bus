/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.spring.autoproxy;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.PatternKit;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;

import java.util.ArrayList;
import java.util.List;

/**
 * Extends {@link BeanNameAutoProxyCreator} to support excluding specified bean names from auto-proxying.
 * <p>
 * This class allows for more fine-grained control over which beans are automatically proxied by Spring AOP. In addition
 * to the standard {@code mappedBeanNames} property, it introduces an {@code excludeBeanNames} property to prevent
 * certain beans from being proxied, even if they match the inclusion patterns.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcludeBeanNameAutoProxy extends BeanNameAutoProxyCreator {

    private List<String> excludeBeanNames;

    /**
     * Sets the names of beans that should NOT be automatically wrapped with a proxy.
     * <p>
     * Names can be specified using a wildcard at the end (e.g., "myBean,tx*") to match beans named "myBean" and all
     * beans whose names start with "tx".
     * </p>
     *
     * @param beanNames An array of bean names to exclude from auto-proxying.
     * @throws IllegalArgumentException if {@code beanNames} is empty.
     * @see org.springframework.beans.factory.FactoryBean
     * @see org.springframework.beans.factory.BeanFactory#FACTORY_BEAN_PREFIX
     */
    public void setExcludeBeanNames(String... beanNames) {
        Assert.notEmpty(beanNames, "'excludeBeanNames' must not be empty");
        this.excludeBeanNames = new ArrayList<>(beanNames.length);
        for (String mappedName : beanNames) {
            this.excludeBeanNames.add(mappedName.strip());
        }
    }

    /**
     * Determines if the given bean name matches any of the configured mapped names and is not excluded by the
     * {@code excludeBeanNames} list.
     *
     * @param beanName   The name of the bean to check.
     * @param mappedName The mapped name pattern to match against.
     * @return {@code true} if the bean matches and is not excluded, {@code false} otherwise.
     */
    @Override
    protected boolean isMatch(String beanName, String mappedName) {
        return super.isMatch(beanName, mappedName) && !isExcluded(beanName);
    }

    /**
     * Checks if the given bean name is present in the {@code excludeBeanNames} list. Supports wildcard matching (e.g.,
     * "tx*").
     *
     * @param beanName The name of the bean to check.
     * @return {@code true} if the bean is excluded, {@code false} otherwise.
     */
    private boolean isExcluded(String beanName) {
        if (excludeBeanNames != null) {
            for (String mappedName : this.excludeBeanNames) {
                if (PatternKit.isMatch(mappedName, beanName)) {
                    return true;
                }
            }
        }
        return false;
    }

}
