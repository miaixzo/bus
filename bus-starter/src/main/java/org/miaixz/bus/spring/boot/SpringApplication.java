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
package org.miaixz.bus.spring.boot;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.spring.boot.statics.BaseStatics;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.GenericTypeResolver;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 扩展{@link org.springframework.boot.SpringApplication}来计算{@link ApplicationContextInitializer}初始化所需的时间。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpringApplication extends org.springframework.boot.SpringApplication {

    private final List<BaseStatics> initializerStartupStatList = new ArrayList<>();

    public SpringApplication(Class<?>... primarySources) {
        super(primarySources);
        // 强制设置自定义监听器
        setRunListeners();
    }

    private void setRunListeners() {
        try {
            // 通过反射访问 listeners 字段
            Field listenersField = org.springframework.boot.SpringApplication.class.getDeclaredField("listeners");
            listenersField.setAccessible(true);
            // 创建只包含 FrameworkRunListener 的列表
            List<org.springframework.boot.SpringApplicationRunListener> listeners = new ArrayList<>();
            listeners.add(new SpringApplicationRunListener(this));
            // 设置监听器列表
            listenersField.set(this, listeners);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set run listeners", e);
        }
    }

    @Override
    public ConfigurableApplicationContext run(String... args) {
        // 在运行前验证监听器
        validateListeners();
        return super.run(args);
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

    // 可选：验证监听器，防止外部篡改
    private void validateListeners() {
        try {
            Field listenersField = org.springframework.boot.SpringApplication.class.getDeclaredField("listeners");
            listenersField.setAccessible(true);
            List<org.springframework.boot.SpringApplicationRunListener> listeners = (List<org.springframework.boot.SpringApplicationRunListener>) listenersField
                    .get(this);
            for (org.springframework.boot.SpringApplicationRunListener listener : listeners) {
                if (!(listener instanceof SpringApplicationRunListener)) {
                    throw new IllegalStateException("Unauthorized listener detected: " + listener.getClass().getName());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate listeners", e);
        }
    }

}
