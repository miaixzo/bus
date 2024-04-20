/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.cache;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import org.miaixz.bus.cache.magic.AbstractReader;
import org.miaixz.bus.cache.magic.MultiCacheReader;
import org.miaixz.bus.cache.magic.SingleCacheReader;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.toolkit.CollKit;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Kimi Liu
 * @since Java 17+
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
        Assert.isTrue(null != config, "config param can not be null.");
        Assert.isTrue(CollKit.isNotEmpty(config.getCaches()), "caches param can not be empty.");

        bind(Context.class).toInstance(config);

        // bind caches
        MapBinder<String, CacheX> mapBinder = MapBinder.newMapBinder(binder(), String.class, CacheX.class);
        config.getCaches().forEach((name, cache) -> mapBinder.addBinding(name).toInstance(cache));

        // bind baseProvider
        Optional.ofNullable(config.getHitting())
                .ifPresent(mxBean -> bind(Hitting.class).toInstance(mxBean));

        bind(AbstractReader.class).annotatedWith(Names.named("singleCacheReader")).to(SingleCacheReader.class);
        bind(AbstractReader.class).annotatedWith(Names.named("multiCacheReader")).to(MultiCacheReader.class);
    }

}
