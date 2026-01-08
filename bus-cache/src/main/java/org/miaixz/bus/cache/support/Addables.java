/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.cache.support;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.miaixz.bus.core.xyz.CollKit;

/**
 * A factory and utility class for creating and manipulating collections, maps, and arrays.
 * <p>
 * This class provides a unified interface, {@link Addable}, for populating different kinds of data structures (Array,
 * Collection, Map) and retrieving the final result.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Addables {

    /**
     * Creates a new {@link Addable} instance based on the target type.
     *
     * @param type The class of the object to create (e.g., {@code List.class}, {@code Map.class},
     *             {@code String[].class}).
     * @param size The initial size or capacity hint.
     * @return An {@link Addable} instance capable of building the specified type.
     */
    public static Addable newAddable(Class<?> type, int size) {
        if (Map.class.isAssignableFrom(type)) {
            return new MapAddable().init((Class<Map>) type, size);
        } else if (Collection.class.isAssignableFrom(type)) {
            return new CollectionAddable().init((Class<Collection>) type, size);
        } else {
            return new ArrayAddable().init((Class<Object[]>) type, size);
        }
    }

    /**
     * Creates a new {@link Collection} instance of a specific type.
     *
     * @param type           The concrete class of the collection to instantiate (e.g., {@code ArrayList.class}).
     * @param initCollection An optional collection whose elements will be added to the new collection.
     * @return A new {@link Collection} instance.
     * @throws RuntimeException if the collection cannot be instantiated via its default constructor.
     */
    public static Collection newCollection(Class<?> type, Collection initCollection) {
        try {
            Collection collection = (Collection) type.getConstructor().newInstance();
            if (CollKit.isNotEmpty(initCollection)) {
                collection.addAll(initCollection);
            }
            return collection;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException e) {
            throw new RuntimeException(
                    "could not invoke collection: " + type.getName() + "'s no param (default) constructor!", e);
        }
    }

    /**
     * Creates a new {@link Map} instance of a specific type.
     *
     * @param type    The concrete class of the map to instantiate (e.g., {@code HashMap.class}).
     * @param initMap An optional map whose entries will be added to the new map.
     * @return A new {@link Map} instance.
     * @throws RuntimeException if the map cannot be instantiated via its default constructor.
     */
    public static Map newMap(Class<?> type, Map initMap) {
        try {
            Map map = (Map) type.getConstructor().newInstance();
            if (CollKit.isNotEmpty(initMap)) {
                map.putAll(initMap);
            }
            return map;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException e) {
            throw new RuntimeException("could not invoke map: " + type.getName() + "'s no param (default) constructor!",
                    e);
        }
    }

    /**
     * An interface for objects that can be built by adding elements to them.
     *
     * @param <T> The type of the object being built.
     */
    public interface Addable<T> {

        /**
         * Initializes the addable with a specific type and initial size.
         *
         * @param type     The class of the object to build.
         * @param initSize The initial size or capacity.
         * @return This {@link Addable} instance for chaining.
         */
        Addable init(Class<T> type, int initSize);

        /**
         * Adds all elements from a list to the object being built.
         *
         * @param list The list of elements to add.
         * @return This {@link Addable} instance for chaining.
         */
        Addable addAll(List<Object> list);

        /**
         * Gets the final, constructed object.
         *
         * @return The built object.
         */
        T get();
    }

    /**
     * An {@link Addable} implementation for building arrays.
     */
    private static class ArrayAddable implements Addable<Object[]> {

        /**
         * The underlying array instance.
         */
        private Object[] instance;

        /**
         * Initializes the array with the specified size.
         *
         * @param type     The array class (e.g., {@code Object[].class}).
         * @param initSize The initial size of the array.
         * @return This {@link Addable} instance for chaining.
         */
        @Override
        public Addable init(Class<Object[]> type, int initSize) {
            this.instance = new Object[initSize];
            return this;
        }

        /**
         * Adds all elements from the list to the array at sequential positions.
         *
         * @param list The list of elements to add to the array.
         * @return This {@link Addable} instance for chaining.
         */
        @Override
        public Addable addAll(List<Object> list) {
            for (int i = 0; i < list.size(); ++i) {
                this.instance[i] = list.get(i);
            }
            return this;
        }

        /**
         * Gets the populated array.
         *
         * @return The populated array.
         */
        @Override
        public Object[] get() {
            return this.instance;
        }
    }

    /**
     * An {@link Addable} implementation for building collections.
     */
    private static class CollectionAddable implements Addable<Collection> {

        /**
         * The underlying collection instance.
         */
        private Collection instance;

        /**
         * Initializes the collection by invoking its no-argument constructor.
         *
         * @param type     The concrete collection class (e.g., {@code ArrayList.class}).
         * @param initSize The initial size hint (currently unused).
         * @return This {@link Addable} instance for chaining.
         * @throws RuntimeException if the collection cannot be instantiated.
         */
        @Override
        public Addable init(Class<Collection> type, int initSize) {
            try {
                this.instance = type.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException e) {
                throw new RuntimeException(
                        "could not invoke collection: " + type.getName() + "'s no param (default) constructor!", e);
            }
            return this;
        }

        /**
         * Adds all elements from the list to the collection.
         *
         * @param list The list of elements to add.
         * @return This {@link Addable} instance for chaining.
         */
        @Override
        public Addable addAll(List<Object> list) {
            this.instance.addAll(list);
            return this;
        }

        /**
         * Gets the populated collection.
         *
         * @return The populated collection.
         */
        @Override
        public Collection get() {
            return this.instance;
        }
    }

    /**
     * An {@link Addable} implementation for building maps.
     */
    private static class MapAddable implements Addable<Map> {

        /**
         * The underlying map instance.
         */
        private Map instance;

        /**
         * Initializes the map by invoking its no-argument constructor.
         *
         * @param type     The concrete map class (e.g., {@code HashMap.class}).
         * @param initSize The initial size hint (currently unused).
         * @return This {@link Addable} instance for chaining.
         * @throws RuntimeException if the map cannot be instantiated.
         */
        @Override
        public Addable init(Class<Map> type, int initSize) {
            try {
                this.instance = type.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                    | InvocationTargetException e) {
                throw new RuntimeException(
                        "could not invoke Map: " + type.getName() + "'s no param (default) constructor!", e);
            }
            return this;
        }

        /**
         * Adds all entries to the map.
         * <p>
         * Each object in the list is expected to be a {@link Map.Entry}.
         * </p>
         *
         * @param list A list of objects, where each object is expected to be a {@link Map.Entry}.
         * @return This {@link Addable} instance for chaining.
         */
        @Override
        public Addable addAll(List<Object> list) {
            if (CollKit.isEmpty(list)) {
                return this;
            }
            list.stream().map(object -> (Map.Entry) object)
                    .forEach(entry -> instance.put(entry.getKey(), entry.getValue()));
            return this;
        }

        /**
         * Gets the populated map.
         *
         * @return The populated map.
         */
        @Override
        public Map get() {
            return instance;
        }
    }

}
