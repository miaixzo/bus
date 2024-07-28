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
package org.miaixz.bus.spring.listener;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.event.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * {@link ApplicationListener}可以使用属性来动态启用侦听器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class SwitchableApplicationListener<E extends ApplicationEvent> implements ApplicationListener<E> {

    @Override
    public void onApplicationEvent(E event) {
        ApplicationContext applicationContext = null;
        Environment environment = null;
        if (event instanceof ApplicationContextEvent applicationContextEvent) {
            applicationContext = applicationContextEvent.getApplicationContext();
        } else if (event instanceof ApplicationContextInitializedEvent applicationContextInitializedEvent) {
            applicationContext = applicationContextInitializedEvent.getApplicationContext();
        } else if (event instanceof ApplicationEnvironmentPreparedEvent environmentPreparedEvent) {
            environment = environmentPreparedEvent.getEnvironment();
        } else if (event instanceof ApplicationPreparedEvent applicationPreparedEvent) {
            applicationContext = applicationPreparedEvent.getApplicationContext();
        } else if (event instanceof ApplicationReadyEvent applicationReadyEvent) {
            applicationContext = applicationReadyEvent.getApplicationContext();
        } else if (event instanceof ApplicationStartedEvent applicationStartedEvent) {
            applicationContext = applicationStartedEvent.getApplicationContext();
        } else if (event instanceof ApplicationFailedEvent applicationFailedEvent) {
            applicationContext = applicationFailedEvent.getApplicationContext();
        }
        if (environment == null && applicationContext != null) {
            environment = applicationContext.getEnvironment();
        }
        if (environment != null) {
            if (isEnable(environment)) {
                doOnApplicationEvent(event);
            }
        } else {
            doOnApplicationEvent(event);
        }
    }

    protected abstract void doOnApplicationEvent(E event);

    /**
     * 从 bus.switch.listener 开始
     *
     * @return 开关键，不能为空。
     */
    protected abstract String switchKey();

    /**
     * 指定如果未设置属性，是否应该匹配条件。默认为{@code true}。
     *
     * @return 如果属性缺失，则条件应该匹配
     */
    protected boolean matchIfMissing() {
        return true;
    }

    protected boolean isEnable(Environment environment) {
        String switchKey = switchKey();
        Assert.hasText(switchKey, "switch key must has text.");
        String realKey = GeniusBuilder.BUS_SWITCH_LISTENER_PREFIX_ + switchKey + ".enabled";
        String switchStr = environment.getProperty(realKey);
        if (StringKit.hasText(switchStr)) {
            return Boolean.parseBoolean(switchStr);
        } else {
            return matchIfMissing();
        }
    }

}
