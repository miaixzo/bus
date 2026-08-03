/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring.bean;

import java.util.Map;
import java.util.Objects;

import org.springframework.core.ResolvableType;

/**
 * Read-only Bean lookup operations bound to one Spring context.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class BeanProvider {

    /**
     * Context owning all lookup operations.
     */
    private final SpringContext context;

    /**
     * Creates a read-only provider for one context.
     *
     * @param context context supplying the active application context
     */
    public BeanProvider(SpringContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    /**
     * Returns a named Bean with its required type.
     *
     * @param <T>  required Bean type
     * @param name Bean name
     * @param type required Bean class
     * @return matching Bean
     */
    public <T> T getBean(String name, Class<T> type) {
        return this.context.get().getBean(name, type);
    }

    /**
     * Returns a named Bean.
     *
     * @param <T>  expected Bean type
     * @param name Bean name
     * @return matching Bean
     */
    public <T> T getBean(String name) {
        return (T) this.context.get().getBean(name);
    }

    /**
     * Returns the unique Bean of a required type.
     *
     * @param <T>  required Bean type
     * @param type required Bean class
     * @return unique matching Bean
     */
    public <T> T getBean(Class<T> type) {
        return this.context.get().getBean(type);
    }

    /**
     * Returns a Bean of a required type, using explicit construction arguments when supplied.
     *
     * @param <T>       required Bean type
     * @param type      required Bean class
     * @param arguments explicit construction arguments
     * @return matching Bean
     */
    public <T> T getBean(Class<T> type, Object... arguments) {
        return arguments == null || arguments.length == 0 ? getBean(type) : this.context.get().getBean(type, arguments);
    }

    /**
     * Returns a named Bean, using explicit construction arguments when supplied.
     *
     * @param <T>       expected Bean type
     * @param name      Bean name
     * @param arguments explicit construction arguments
     * @return matching Bean
     */
    public <T> T getBean(String name, Object... arguments) {
        return (T) (arguments == null || arguments.length == 0 ? this.context.get().getBean(name)
                : this.context.get().getBean(name, arguments));
    }

    /**
     * Returns all Beans of a type keyed by Bean name.
     *
     * @param <T>  required Bean type
     * @param type required Bean class
     * @return immutable map of names to matching Beans
     */
    public <T> Map<String, T> getBeansOfType(Class<T> type) {
        return Map.copyOf(this.context.get().getBeansOfType(type));
    }

    /**
     * Returns all Bean names assignable to a type.
     *
     * @param type required Bean class
     * @return matching Bean names
     */
    public String[] getBeanNamesForType(Class<?> type) {
        return this.context.get().getBeanNamesForType(type);
    }

    /**
     * Returns all Bean names assignable to a resolvable generic type.
     *
     * @param type required resolvable type
     * @return matching Bean names
     */
    public String[] getBeanNamesForType(ResolvableType type) {
        return this.context.get().getBeanNamesForType(type);
    }

    /**
     * Returns whether the context contains the named Bean.
     *
     * @param name Bean name
     * @return {@code true} when the Bean is available
     */
    public boolean containsBean(String name) {
        return this.context.get().containsBean(name);
    }

}
