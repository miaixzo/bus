/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.cache;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import org.aoju.bus.cache.reader.AbstractReader;
import org.aoju.bus.cache.reader.MultiCacheReader;
import org.aoju.bus.cache.reader.SingleCacheReader;
import org.aoju.bus.cache.support.cache.Cache;
import org.aoju.bus.core.utils.CollUtils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Kimi Liu
 * @version 5.3.3
 * @since JDK 1.8+
 */
public class Module extends AbstractModule {

    private static final AtomicBoolean init = new AtomicBoolean(false);

    private static Injector injector;

    private Context config;

    private Module(Context config) {
        this.config = config;
    }

    public synchronized static Complex coreInstance(Context config) {
        if (init.compareAndSet(false, true)) {
            injector = Guice.createInjector(new Module(config));
        }
        return injector.getInstance(Complex.class);
    }

    /**
     * 所有bean的装配工作都放到这儿
     */
    @Override
    protected void configure() {
        Preconditions.checkArgument(config != null, "config param can not be null.");
        Preconditions.checkArgument(CollUtils.isNotEmpty(config.getCaches()), "caches param can not be empty.");

        bind(Context.class).toInstance(config);

        // bind caches
        MapBinder<String, Cache> mapBinder = MapBinder.newMapBinder(binder(), String.class, Cache.class);
        config.getCaches().forEach((name, cache) -> mapBinder.addBinding(name).toInstance(cache));

        // bind baseProvider
        Optional.ofNullable(config.getProvider())
                .ifPresent(mxBean -> bind(Provider.class).toInstance(mxBean));

        bind(AbstractReader.class).annotatedWith(Names.named("singleCacheReader")).to(SingleCacheReader.class);
        bind(AbstractReader.class).annotatedWith(Names.named("multiCacheReader")).to(MultiCacheReader.class);
    }

}
