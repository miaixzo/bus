package org.aoju.bus.cache;

import org.aoju.bus.cache.provider.BaseProvider;
import org.aoju.bus.cache.reader.AbstractCacheReader;
import org.aoju.bus.cache.reader.MultiCacheReader;
import org.aoju.bus.cache.reader.SingleCacheReader;
import org.aoju.bus.cache.support.cache.Cache;
import org.aoju.bus.core.utils.CollUtils;
import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class CacheModule extends AbstractModule {

    private static final AtomicBoolean init = new AtomicBoolean(false);

    private static Injector injector;

    private CacheConfig config;

    private CacheModule(CacheConfig config) {
        this.config = config;
    }

    public synchronized static CacheCore coreInstance(CacheConfig config) {
        if (init.compareAndSet(false, true)) {
            injector = Guice.createInjector(new CacheModule(config));
        }
        return injector.getInstance(CacheCore.class);
    }

    /**
     * 所有bean的装配工作都放到这儿
     */
    @Override
    protected void configure() {
        Preconditions.checkArgument(config != null, "config param can not be null.");
        Preconditions.checkArgument(CollUtils.isNotEmpty(config.getCaches()), "caches param can not be empty.");

        bind(CacheConfig.class).toInstance(config);

        // bind caches
        MapBinder<String, Cache> mapBinder = MapBinder.newMapBinder(binder(), String.class, Cache.class);
        config.getCaches().forEach((name, cache) -> mapBinder.addBinding(name).toInstance(cache));

        // bind baseProvider
        Optional.ofNullable(config.getProvider())
                .ifPresent(mxBean -> bind(BaseProvider.class).toInstance(mxBean));

        bind(AbstractCacheReader.class).annotatedWith(Names.named("singleCacheReader")).to(SingleCacheReader.class);
        bind(AbstractCacheReader.class).annotatedWith(Names.named("multiCacheReader")).to(MultiCacheReader.class);
    }

}
