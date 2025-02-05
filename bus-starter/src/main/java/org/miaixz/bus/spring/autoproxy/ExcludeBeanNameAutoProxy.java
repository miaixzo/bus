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

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.PatternKit;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;

/**
 * 扩展{@link BeanNameAutoProxyCreator}以支持排除指定的bean名称。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcludeBeanNameAutoProxy extends BeanNameAutoProxyCreator {

    private List<String> excludeBeanNames;

    /**
     * 设置不应该自动用代理包装的bean的名称。 名称可以以"*"结尾指定要匹配的前缀，例如:“myBean,tx*”将匹配名为“myBean”的bean和所有名称以“tx”开头的bean。
     *
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

    @Override
    protected boolean isMatch(String beanName, String mappedName) {
        return super.isMatch(beanName, mappedName) && !isExcluded(beanName);
    }

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
