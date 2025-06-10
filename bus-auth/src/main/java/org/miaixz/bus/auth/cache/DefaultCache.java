package org.miaixz.bus.auth.cache;

import java.time.Duration;
import java.util.Objects;

import org.miaixz.bus.logger.Logger;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.shareddata.AsyncMap;

public class DefaultCache implements Cache {
    private final AsyncMap<String, String> cache;

    private DefaultCache(AsyncMap<String, String> cache) {
        this.cache = cache;
    }

    public static Future<Cache> build(Vertx vertx) {
        if (vertx.isClustered()) {
            return vertx.sharedData().<String, String>getClusterWideMap("clusterCache")
                    .map(cache -> (Cache) new DefaultCache(cache))
                    .onFailure(cause -> Logger.error("ClusterCache initialize error", cause));
        } else {
            return vertx.sharedData().<String, String>getLocalAsyncMap("clusterCache")
                    .map(cache -> (Cache) new DefaultCache(cache))
                    .onFailure(cause -> Logger.error("ClusterCache initialize error", cause));
        }
    }

    @Override
    public Future<Boolean> exists(String key) {
        return cache.get(key).map(Objects::nonNull);
    }

    @Override
    public Future<Boolean> delete(String key) {
        return cache.remove(key).map(Objects::nonNull);
    }

    @Override
    public <T> Future<Void> set(String key, T obj) {
        return cache.put(key, Json.encode(obj));
    }

    @Override
    public <T> Future<Void> set(String key, T obj, Duration expire) {
        return cache.put(key, Json.encode(obj), expire.toMillis());
    }

    @Override
    public <T> Future<@Nullable T> get(String key, Class<T> clazz) {
        return cache.get(key).map(v -> Json.decodeValue(v, clazz));
    }

}
