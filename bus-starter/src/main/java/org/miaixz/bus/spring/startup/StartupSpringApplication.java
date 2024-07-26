/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.spring.startup;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.spring.startup.statics.BaseStatics;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.GenericTypeResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * 扩展{@link SpringApplication}来计算{@link ApplicationContextInitializer}初始化所需的时间。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StartupSpringApplication extends SpringApplication {

    private final List<BaseStatics> initializerStartupStatList = new ArrayList<>();

    public StartupSpringApplication(Class<?>... primarySources) {
        super(primarySources);
    }

    @Override
    protected void applyInitializers(ConfigurableApplicationContext context) {
        for (ApplicationContextInitializer initializer : getInitializers()) {
            Class<?> requiredType = GenericTypeResolver.resolveTypeArgument(initializer.getClass(),
                    ApplicationContextInitializer.class);
            if (requiredType != null) {
                Assert.isInstanceOf(requiredType, context, "Unable to call initializer.");
                BaseStatics stat = new BaseStatics();
                stat.setName(initializer.getClass().getName());
                stat.setStartTime(System.currentTimeMillis());
                initializer.initialize(context);
                stat.setEndTime(System.currentTimeMillis());
                initializerStartupStatList.add(stat);
            }
        }
    }

    public List<BaseStatics> getInitializerStartupStatList() {
        return initializerStartupStatList;
    }

}
