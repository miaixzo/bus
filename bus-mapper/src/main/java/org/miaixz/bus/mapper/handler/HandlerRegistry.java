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
package org.miaixz.bus.mapper.handler;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
 * List<MapperHandler> queryHandlers = manager.getHandlers(HandlerType.QUERY);
 * List<MapperHandler> updateHandlers = manager.getHandlers(HandlerType.UPDATE);
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HandlerRegistry {

    /**
     * Cache of handler classes to their overridden methods to avoid repeated reflection checks
     */
    private static final Map<Class<?>, EnumSet<HandlerType>> METHOD_CACHE = new ConcurrentHashMap<>();
    /**
     * Handler list indexed by operation type, using EnumMap for optimal performance
     */
    private final EnumMap<HandlerType, List<MapperHandler>> handlerIndex = new EnumMap<>(HandlerType.class);

    /**
     * Set of all handlers (deduplicated)
     */
    private final Set<MapperHandler> handlers = new LinkedHashSet<>();

    /**
     * Constructor that initializes the index table.
     */
    public HandlerRegistry() {
        for (HandlerType type : HandlerType.values()) {
            handlerIndex.put(type, new ArrayList<>());
        }
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
    public void addHandler(MapperHandler handler) {
        if (handler == null) {
            return;
        }

        // Deduplicate: don't add if already exists
        if (!handlers.add(handler)) {
            return;
        }

        // Detect which methods the handler overrides
        EnumSet<HandlerType> overriddenTypes = detectOverriddenMethods(handler);

        // Build index based on overridden method types
        for (HandlerType type : overriddenTypes) {
            handlerIndex.get(type).add(handler);
        }

        // Sort all handler lists by order value
        sortHandlers();
    }

    /**
     * Sorts all handler lists by their order values.
     */
    private void sortHandlers() {
        for (List<MapperHandler> handlers : handlerIndex.values()) {
            handlers.sort(Comparator.comparingInt(MapperHandler::getOrder));
        }
    }

    /**
     * Adds multiple handlers in batch.
     *
     * @param handlers the handler list
     */
    public void addHandlers(Collection<MapperHandler> handlers) {
        if (handlers != null) {
            handlers.forEach(this::addHandler);
        }
    }

    /**
     * Gets handlers of the specified type.
     *
     * @param type the handler type
     * @return an unmodifiable list of handlers
     */
    public List<MapperHandler> getHandlers(HandlerType type) {
        List<MapperHandler> handlers = handlerIndex.get(type);
        return handlers != null ? Collections.unmodifiableList(handlers) : Collections.emptyList();
    }

    /**
     * Gets all handlers.
     *
     * @return an unmodifiable set of all handlers
     */
    public Set<MapperHandler> getHandlers() {
        return Collections.unmodifiableSet(handlers);
    }

    /**
     * Clears all handlers.
     */
    public void clear() {
        handlers.clear();
        handlerIndex.values().forEach(List::clear);
    }

    /**
     * Gets the total number of handlers.
     *
     * @return the total number of handlers
     */
    public int size() {
        return handlers.size();
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
    private EnumSet<HandlerType> detectOverriddenMethods(MapperHandler handler) {
        Class<?> handlerClass = handler.getClass();

        // Try to get from cache first
        EnumSet<HandlerType> cached = METHOD_CACHE.get(handlerClass);
        if (cached != null) {
            return cached;
        }

        // Detect overridden methods
        EnumSet<HandlerType> overriddenTypes = EnumSet.noneOf(HandlerType.class);

        // Detect isQuery/query methods
        if (isMethodOverridden(handlerClass, "isQuery") || isMethodOverridden(handlerClass, "query")) {
            overriddenTypes.add(HandlerType.QUERY);
        }

        // Detect isUpdate/update methods
        if (isMethodOverridden(handlerClass, "isUpdate") || isMethodOverridden(handlerClass, "update")) {
            overriddenTypes.add(HandlerType.UPDATE);
        }

        // Detect prepare method
        if (isMethodOverridden(handlerClass, "prepare")) {
            overriddenTypes.add(HandlerType.PREPARE);
        }

        // Detect getBoundSql method
        if (isMethodOverridden(handlerClass, "getBoundSql")) {
            overriddenTypes.add(HandlerType.GET_BOUND_SQL);
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
        StringBuilder sb = new StringBuilder();
        sb.append("HandlerRegistry Statistics:\n");
        sb.append("  Total handlers: ").append(handlers.size()).append("\n");

        for (HandlerType type : HandlerType.values()) {
            int count = handlerIndex.get(type).size();
            sb.append("  ").append(type).append(": ").append(count).append(" handlers\n");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "HandlerRegistry{" + "totalHandlers=" + handlers.size() + ", queryHandlers="
                + handlerIndex.get(HandlerType.QUERY).size() + ", updateHandlers="
                + handlerIndex.get(HandlerType.UPDATE).size() + ", prepareHandlers="
                + handlerIndex.get(HandlerType.PREPARE).size() + ", getBoundSqlHandlers="
                + handlerIndex.get(HandlerType.GET_BOUND_SQL).size() + '}';
    }

    /**
     * Handler type enumeration.
     */
    public enum HandlerType {
        /**
         * Query operation (isQuery/query methods)
         */
        QUERY,

        /**
         * Update operation (isUpdate/update methods)
         */
        UPDATE,

        /**
         * Prepare operation (prepare method)
         */
        PREPARE,

        /**
         * GetBoundSql operation (getBoundSql method)
         */
        GET_BOUND_SQL
    }

}
