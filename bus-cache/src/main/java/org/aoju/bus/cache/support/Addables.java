/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.cache.support;

import org.aoju.bus.core.utils.CollUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 5.6.3
 * @since JDK 1.8+
 */
public class Addables {

    public static Addable newAddable(Class<?> type, int size) {
        if (Map.class.isAssignableFrom(type)) {
            return new MapAddable().init((Class<Map>) type, size);
        } else if (Collection.class.isAssignableFrom(type)) {
            return new CollectionAddable().init((Class<Collection>) type, size);
        } else {
            return new ArrayAddable().init((Class<Object[]>) type, size);
        }
    }

    public static Collection newCollection(Class<?> type, Collection initCollection) {
        try {
            Collection collection = (Collection) type.newInstance();
            if (CollUtils.isNotEmpty(initCollection)) {
                collection.addAll(initCollection);
            }

            return collection;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("could not invoke collection: " + type.getName() + "'s no param (default) constructor!", e);
        }
    }

    public static Map newMap(Class<?> type, Map initMap) {
        try {
            Map map = (Map) type.newInstance();
            if (CollUtils.isNotEmpty(initMap)) {
                map.putAll(initMap);
            }
            return map;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("could not invoke map: " + type.getName() + "'s no param (default) constructor!", e);
        }
    }

    public interface Addable<T> {

        Addable init(Class<T> type, int initSize);

        Addable addAll(List<Object> list);

        T getResult();
    }

    private static class ArrayAddable implements Addable<Object[]> {

        private Object[] instance;

        @Override
        public Addable init(Class<Object[]> type, int initSize) {
            this.instance = new Object[initSize];
            return this;
        }

        @Override
        public Addable addAll(List<Object> list) {
            for (int i = 0; i < list.size(); ++i) {
                this.instance[i] = list.get(i);
            }

            return this;
        }

        @Override
        public Object[] getResult() {
            return this.instance;
        }
    }

    private static class CollectionAddable implements Addable<Collection> {

        private Collection instance;

        @Override
        public Addable init(Class<Collection> type, int initSize) {
            try {
                this.instance = type.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("could not invoke collection: " + type.getName() + "'s no param (default) constructor!", e);
            }

            return this;
        }

        @Override
        public Addable addAll(List<Object> list) {
            this.instance.addAll(list);
            return this;
        }

        @Override
        public Collection getResult() {
            return this.instance;
        }
    }

    private static class MapAddable implements Addable<Map> {

        private Map instance;

        @Override
        public Addable init(Class<Map> type, int initSize) {
            try {
                this.instance = type.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("could not invoke Map: " + type.getName() + "'s no param (default) constructor!", e);
            }

            return this;
        }

        @Override
        public Addable addAll(List<Object> list) {
            if (CollUtils.isEmpty(list)) {
                return this;
            }

            list.stream().map(obj -> (Map.Entry) obj).forEach(entry -> instance.put(entry.getKey(), entry.getValue()));

            return this;
        }

        @Override
        public Map getResult() {
            return instance;
        }
    }
}
