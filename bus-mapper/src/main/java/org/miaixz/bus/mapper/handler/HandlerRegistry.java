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
package org.miaixz.bus.mapper.handler;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Charter.Handler;

/**
 * Indexed handler manager that optimizes handler lookup performance.
 *
 * <p>
 * Optimizes handler lookup from O(n) traversal to O(1) indexed lookup.
 * </p>
 *
 * <p>
 * Performance comparison (100 handlers scenario):
 * </p>
 * <ul>
 * <li>Before optimization: Traverse all handlers, average 50 comparisons</li>
 * <li>After optimization: Direct index lookup, 1 lookup</li>
 * <li>Performance improvement: Approximately 50x faster</li>
 * </ul>
 *
 * <p>
 * Implementation principles:
 * </p>
 * <ol>
 * <li>During registration: Detect which methods the handler actually overrides (non-default implementations)</li>
 * <li>Build index: Categorize and store by operation type (QUERY, UPDATE, PREPARE, GET_BOUND_SQL)</li>
 * <li>During lookup: Directly retrieve the corresponding handler list based on operation type</li>
 * </ol>
 *
 * <p>
 * Usage example:
 * </p>
 *
 * <pre>{@code
 * HandlerRegistry manager = new HandlerRegistry();
 *
 * // Add handlers
 * manager.addHandler(new MyQueryHandler());
 * manager.addHandler(new MyUpdateHandler());
 *
 * // Fast lookup (O(1))
 * List<MapperHandler> queryHandlers = manager.getHandlers(Handler.QUERY);
 * List<MapperHandler> updateHandlers = manager.getHandlers(Handler.UPDATE);
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HandlerRegistry {

    /**
     * Cache of handler classes to their overridden methods to avoid repeated reflection checks
     */
    private static final Map<Class<?>, EnumSet<Handler>> METHOD_CACHE = new ConcurrentHashMap<>();

    /**
     * Set of all handlers (deduplicated)
     */
    private final Set<MapperHandler> handlers = new LinkedHashSet<>();

    /**
     * Immutable handler snapshot published to runtime readers.
     */
    private final AtomicReference<HandlerSnapshot> snapshot = new AtomicReference<>(HandlerSnapshot.empty());

    /**
     * Constructor that initializes the index table.
     */
    public HandlerRegistry() {
        publishSnapshot();
    }

    /**
     * Adds a handler.
     *
     * <p>
     * Handlers are automatically sorted by their {@link MapperHandler#getOrder()} value after being added. Lower order
     * values execute first.
     * </p>
     *
     * @param handler the handler instance
     */
    public synchronized void addHandler(MapperHandler handler) {
        if (handler == null) {
            return;
        }

        // Deduplicate: don't add if already exists
        if (!handlers.add(handler)) {
            return;
        }

        publishSnapshot();
        HandlerSnapshot current = snapshot.get();
        Logger.debug(
                false,
                "Mapper",
                "Mapper handler registered: handler={}, order={}, queryHandlers={}, updateHandlers={}, prepareHandlers={}, boundSqlHandlers={}, totalHandlers={}",
                handler.getClass().getName(),
                handler.getOrder(),
                current.getHandlers(Handler.QUERY).size(),
                current.getHandlers(Handler.UPDATE).size(),
                current.getHandlers(Handler.PREPARE).size(),
                current.getHandlers(Handler.GET_BOUND_SQL).size(),
                current.size());
    }

    /**
     * Adds multiple handlers in batch.
     *
     * @param handlers the handler list
     */
    public synchronized void addHandlers(Collection<MapperHandler> handlers) {
        if (handlers == null || handlers.isEmpty()) {
            return;
        }
        boolean changed = false;
        for (MapperHandler handler : handlers) {
            if (handler != null) {
                changed |= this.handlers.add(handler);
            }
        }
        if (changed) {
            publishSnapshot();
        }
    }

    /**
     * Gets handlers of the specified type.
     *
     * @param type the handler type
     * @return an unmodifiable list of handlers
     */
    public List<MapperHandler> getHandlers(Handler type) {
        return snapshot.get().getHandlers(type);
    }

    /**
     * Gets all handlers.
     *
     * @return an unmodifiable set of all handlers
     */
    public Set<MapperHandler> getHandlers() {
        return snapshot.get().getHandlers();
    }

    /**
     * Clears all handlers.
     */
    public synchronized void clear() {
        handlers.clear();
        publishSnapshot();
        Logger.debug(false, "Mapper", "Mapper handler registry cleared");
    }

    /**
     * Gets the total number of handlers.
     *
     * @return the total number of handlers
     */
    public int size() {
        return snapshot.get().size();
    }

    /**
     * Builds and publishes a new immutable runtime snapshot.
     */
    private void publishSnapshot() {
        EnumMap<Handler, List<MapperHandler>> index = new EnumMap<>(Handler.class);
        for (Handler type : Handler.values()) {
            index.put(type, new ArrayList<>());
        }
        for (MapperHandler handler : handlers) {
            EnumSet<Handler> overriddenTypes = detectOverriddenMethods(handler);
            for (Handler type : overriddenTypes) {
                index.get(type).add(handler);
            }
        }
        for (List<MapperHandler> indexedHandlers : index.values()) {
            indexedHandlers.sort(Comparator.comparingInt(MapperHandler::getOrder));
        }
        snapshot.set(new HandlerSnapshot(index, handlers));
    }

    /**
     * Detects which methods the handler actually overrides.
     *
     * <p>
     * Strategy:
     * </p>
     * <ol>
     * <li>First try to get from cache (avoid repeated reflection)</li>
     * <li>Use reflection to detect method declaring class:
     * <ul>
     * <li>If declaring class is MapperHandler interface → uses default implementation (not indexed)</li>
     * <li>If declaring class is concrete class → overridden (needs indexing)</li>
     * </ul>
     * </li>
     * <li>Cache the result for subsequent use</li>
     * </ol>
     *
     * @param handler the handler instance
     * @return the set of overridden method types
     */
    private EnumSet<Handler> detectOverriddenMethods(MapperHandler handler) {
        Class<?> handlerClass = handler.getClass();

        // Try to get from cache first
        EnumSet<Handler> cached = METHOD_CACHE.get(handlerClass);
        if (cached != null) {
            return cached;
        }

        // Detect overridden methods
        EnumSet<Handler> overriddenTypes = EnumSet.noneOf(Handler.class);

        // Detect isQuery/query methods
        if (isMethodOverridden(handlerClass, "isQuery") || isMethodOverridden(handlerClass, "query")) {
            overriddenTypes.add(Handler.QUERY);
        }

        // Detect isUpdate/update methods
        if (isMethodOverridden(handlerClass, "isUpdate") || isMethodOverridden(handlerClass, "update")) {
            overriddenTypes.add(Handler.UPDATE);
        }

        // Detect prepare method
        if (isMethodOverridden(handlerClass, "prepare")) {
            overriddenTypes.add(Handler.PREPARE);
        }

        // Detect getBoundSql method
        if (isMethodOverridden(handlerClass, "getBoundSql")) {
            overriddenTypes.add(Handler.GET_BOUND_SQL);
        }

        // Cache the result
        METHOD_CACHE.put(handlerClass, overriddenTypes);

        return overriddenTypes;
    }

    /**
     * Checks if a method is overridden.
     *
     * @param handlerClass the handler class
     * @param methodName   the method name
     * @return true if the method is overridden
     */
    private boolean isMethodOverridden(Class<?> handlerClass, String methodName) {
        try {
            // Get method by name
            Method[] methods = handlerClass.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    // Check method declaring class
                    Class<?> declaringClass = method.getDeclaringClass();

                    // If declaring class is MapperHandler interface, it uses default implementation
                    if (declaringClass.equals(MapperHandler.class)) {
                        return false;
                    }

                    // If declaring class is concrete class, it's overridden
                    return true;
                }
            }
        } catch (Exception e) {
            Logger.warn(
                    false,
                    "Mapper",
                    e,
                    "Mapper operation failed: provider={}, exception={}",
                    "HandlerRegistry",
                    e.getClass().getSimpleName());
            // Conservative handling on exception: assume not overridden
            return false;
        }

        return false;
    }

    /**
     * Gets index statistics.
     *
     * @return statistics information string
     */
    public String getStatistics() {
        HandlerSnapshot current = snapshot.get();
        StringBuilder sb = new StringBuilder();
        sb.append("HandlerRegistry Statistics:\n");
        sb.append("  Total handlers: ").append(current.size()).append("\n");

        for (Handler type : Handler.values()) {
            int count = current.getHandlers(type).size();
            sb.append("  ").append(type).append(": ").append(count).append(" handlers\n");
        }

        return sb.toString();
    }

    /**
     * Returns a readable representation of the registered handler groups.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        HandlerSnapshot current = snapshot.get();
        return "HandlerRegistry{" + "totalHandlers=" + current.size() + ", queryHandlers="
                + current.getHandlers(Handler.QUERY).size() + ", updateHandlers="
                + current.getHandlers(Handler.UPDATE).size() + ", prepareHandlers="
                + current.getHandlers(Handler.PREPARE).size() + ", getBoundSqlHandlers="
                + current.getHandlers(Handler.GET_BOUND_SQL).size() + '}';
    }

    /**
     * Immutable runtime view of registered handlers.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static final class HandlerSnapshot {

        /**
         * Handler lists indexed by operation type.
         */
        private final Map<Handler, List<MapperHandler>> handlerIndex;

        /**
         * All registered handlers.
         */
        private final Set<MapperHandler> handlers;

        /**
         * Creates a snapshot from mutable handler state.
         *
         * @param handlerIndex the indexed handler lists
         * @param handlers     all registered handlers
         */
        private HandlerSnapshot(Map<Handler, List<MapperHandler>> handlerIndex, Set<MapperHandler> handlers) {
            EnumMap<Handler, List<MapperHandler>> index = new EnumMap<>(Handler.class);
            for (Handler type : Handler.values()) {
                List<MapperHandler> indexedHandlers = handlerIndex.get(type);
                index.put(type, indexedHandlers == null ? List.of() : List.copyOf(indexedHandlers));
            }
            this.handlerIndex = Map.copyOf(index);
            this.handlers = Set.copyOf(handlers);
        }

        /**
         * Creates an empty snapshot.
         *
         * @return the empty snapshot
         */
        private static HandlerSnapshot empty() {
            EnumMap<Handler, List<MapperHandler>> index = new EnumMap<>(Handler.class);
            for (Handler type : Handler.values()) {
                index.put(type, List.of());
            }
            return new HandlerSnapshot(index, Set.of());
        }

        /**
         * Gets handlers of the specified type.
         *
         * @param type the handler type
         * @return the immutable handler list
         */
        private List<MapperHandler> getHandlers(Handler type) {
            return handlerIndex.getOrDefault(type, List.of());
        }

        /**
         * Gets all registered handlers.
         *
         * @return the immutable handler set
         */
        private Set<MapperHandler> getHandlers() {
            return handlers;
        }

        /**
         * Gets the total handler count.
         *
         * @return the handler count
         */
        private int size() {
            return handlers.size();
        }

    }

}
