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
package org.miaixz.bus.core.lang.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.miaixz.bus.core.Loader;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.loader.LazyFunLoader;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.ThreadKit;

/**
 * 简单的事件发布者实现，基于{@link Subscriber}和{@link Event}实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SimpleEventPublisher implements EventPublisher {

    private final List<Subscriber> subscribers;
    private Loader<ExecutorService> executorServiceLoader;

    /**
     * 构造
     *
     * @param subscribers           订阅者列表
     * @param executorServiceLoader 线程池加载器，用于异步执行，默认为{@link ThreadKit#newExecutor()}
     */
    public SimpleEventPublisher(final List<Subscriber> subscribers,
            final Loader<ExecutorService> executorServiceLoader) {
        this.subscribers = ObjectKit.defaultIfNull(subscribers, ArrayList::new);
        this.executorServiceLoader = ObjectKit.defaultIfNull(executorServiceLoader,
                LazyFunLoader.of(ThreadKit::newExecutor));
    }

    /**
     * 创建一个默认的{@code SimpleEventPublisher}，默认线程池为{@link ThreadKit#newExecutor()}
     *
     * @return {@code SimpleEventPublisher}
     */
    public static SimpleEventPublisher of() {
        return of(null);
    }

    /**
     * 创建一个默认的{@code SimpleEventPublisher}，默认线程池为{@link ThreadKit#newExecutor()}
     *
     * @param subscribers 订阅者列表，也可以传入空列表后调用{@link #register(Subscriber)}添加
     * @return {@code SimpleEventPublisher}
     */
    public static SimpleEventPublisher of(final List<Subscriber> subscribers) {
        return new SimpleEventPublisher(subscribers, null);
    }

    /**
     * 设置自定义的{@link ExecutorService}线程池，默认为{@link ThreadKit#newExecutor()}
     *
     * @param executorService {@link ExecutorService}，不能为空
     * @return this
     */
    public SimpleEventPublisher setExecutorService(final ExecutorService executorService) {
        this.executorServiceLoader = () -> Assert.notNull(executorService);
        return this;
    }

    @Override
    public EventPublisher register(final Subscriber subscriber) {
        subscribers.add(subscriber);
        Collections.sort(subscribers);
        return this;
    }

    @Override
    public void publish(final Event event) {
        for (final Subscriber subscriber : subscribers) {
            if (subscriber.async()) {
                executorServiceLoader.get().submit(() -> subscriber.update(event));
            } else {
                subscriber.update(event);
            }
        }
    }

}
