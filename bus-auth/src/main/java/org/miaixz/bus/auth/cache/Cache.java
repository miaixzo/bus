package org.miaixz.bus.auth.cache;

import java.time.Duration;

import io.vertx.core.Future;

public interface Cache {

    Future<Boolean> exists(String key);

    Future<Boolean> delete(String key);

    <T> Future<Void> set(String key, T obj);

    <T> Future<Void> set(String key, T obj, Duration expire);

    <T> Future<T> get(String key, Class<T> clazz);

}
