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
package org.miaixz.bus.spring.aot;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.logger.Logger;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.support.RequestContext;

/**
 * Centralized registration of {@link RuntimeHints} for Spring Native AOT (Ahead-of-Time) compilation.
 * <p>
 * This class provides a modern fluent API for registering runtime hints needed by GraalVM Native Image.
 * <p>
 * <strong>Architecture Overview:</strong>
 *
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ Application Code (Before AOT Compilation)                       │
 * │ NativeImageRuntimeHints.hints()                                   │
 * │     .reflectOn(User.class, Order.class)                         │
 * │     .proxyFor(UserMapper.class)                                 │
 * │     .resources("**&#47;*.xml")                                  │
 * │     .register();                                                │
 * └────────────────────────┬────────────────────────────────────────┘
 *                          │ Deferred to static queues
 *                          ↓
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ Spring AOT Build Process (native-image compilation)             │
 * │ registerHints() called by Spring                                │
 * │   ├─ Process deferred registrations from queues                 │
 * │   ├─ Register framework-level hints (RequestContext, etc.)      │
 * │   └─ Write to native-image configuration files                  │
 * └────────────────────────┬────────────────────────────────────────┘
 *                          │ Compiled into native image
 *                          ↓
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ Native Image Runtime                                            │
 * │ All registered classes/proxies/resources available for use      │
 * └─────────────────────────────────────────────────────────────────┘
 * </pre>
 * <p>
 * <strong>Usage Examples:</strong>
 *
 * <pre>
 * &#64;Configuration
 * public class MyConfiguration {
 *
 *     static {
 *         // Example 1: Basic entity and mapper registration
 *         NativeImageRuntimeHints.hints().reflectOn(User.class, Order.class).proxyFor(UserMapper.class).register();
 *
 *         // Example 2: Complete configuration
 *         NativeImageRuntimeHints.hints().reflectOn(User.class, Order.class, Product.class)
 *                 .proxyFor(UserMapper.class, OrderMapper.class).resources("custom/**&#47;*.xml", "templates/**&#47;*.ftl")
 *                 .handlers("com.example.CustomHandler", "com.example.AnotherHandler").register();
 *     }
 * }
 * </pre>
 * <p>
 * <strong>Important Notes:</strong>
 * <ul>
 * <li>All registrations via fluent API are deferred and processed during AOT compilation</li>
 * <li>Thread-safe: All builder operations use synchronized blocks</li>
 * <li>Idempotent: Registering the same class multiple times is safe (Spring handles deduplication)</li>
 * <li>JVM Mode: Zero runtime overhead in JVM mode (only used during native-image compilation)</li>
 * <li>Must call {@code .register()} to finalize all registrations</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NativeImageRuntimeHints implements RuntimeHintsRegistrar {

    /**
     * Queue for classes requiring full reflection access in GraalVM Native Image.
     */
    private static final List<Class<?>> deferredReflectionClasses = ListKit.of();

    /**
     * Queue for JDK dynamic proxy interface combinations.
     */
    private static final List<Class<?>[]> deferredProxyInterfaces = ListKit.of();

    /**
     * Queue for resource file patterns to include in Native Image binary.
     */
    private static final List<String> deferredResourcePatterns = ListKit.of();

    /**
     * Queue for handler class names resolved dynamically at AOT processing time.
     */
    private static final List<String> deferredHandlerClassNames = ListKit.of();

    /**
     * Registers runtime hints for GraalVM Native Image compilation.
     *
     * @param hints       The {@link RuntimeHints} instance to register hints with
     * @param classLoader The application's class loader for loading classes by name
     */
    @Override
    public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
        if (classLoader == null) {
            Logger.warn("ClassLoader is null, skipping hint registration");
            return;
        }

        Logger.debug("Starting runtime hints registration");
        processDeferredRegistrations(hints, classLoader);
        registerFrameworkHints(hints);
        Logger.debug("Runtime hints registration completed");
    }

    /**
     * Processes all deferred registrations queued via the fluent API.
     */
    private synchronized void processDeferredRegistrations(RuntimeHints hints, ClassLoader classLoader) {
        // Process deferred reflection classes
        for (Class<?> clazz : deferredReflectionClasses) {
            hints.reflection().registerType(clazz, MemberCategory.values());
            Logger.debug("Registered reflection class: {}", clazz.getName());
        }

        // Process deferred proxy interfaces
        for (Class<?>[] proxyInterfaces : deferredProxyInterfaces) {
            hints.proxies().registerJdkProxy(proxyInterfaces);
            Logger.debug(
                    "Registered proxy interfaces: {}",
                    Arrays.stream(proxyInterfaces).map(Class::getName).toList());
        }

        // Process deferred resource patterns
        for (String pattern : deferredResourcePatterns) {
            hints.resources().registerPattern(pattern);
            Logger.debug("Registered resource pattern: {}", pattern);
        }

        // Process deferred handler class names
        for (String className : deferredHandlerClassNames) {
            try {
                Class<?> handlerClass = classLoader.loadClass(className);
                hints.reflection().registerType(handlerClass, MemberCategory.values());
                Logger.debug("Registered handler class: {}", className);
            } catch (ClassNotFoundException e) {
                Logger.warn("Failed to register handler: {}", className, e);
            }
        }
    }

    /**
     * Registers framework-level runtime hints for core library classes.
     */
    private void registerFrameworkHints(RuntimeHints hints) {
        try {
            Method method = RequestContext.class.getMethod("getContextPath");
            hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
            Logger.debug("Registered framework method: RequestContext.getContextPath()");
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "Failed to find RequestContext.getContextPath() method for AOT hint registration. "
                            + "This may indicate a Spring Framework version incompatibility.",
                    e);
        }
    }

    /**
     * Creates a new fluent builder for registering runtime hints.
     * <p>
     * <strong>Usage Example:</strong>
     *
     * <pre>
     * NativeImageRuntimeHints.hints().reflectOn(User.class, Order.class).proxyFor(UserMapper.class).resources("**&#47;*.xml")
     *         .register();
     * </pre>
     *
     * @return A new {@link HintBuilder} instance for chaining registration calls
     */
    public static HintBuilder hints() {
        return new HintBuilder();
    }

    /**
     * Fluent builder for registering runtime hints with chainable API.
     * <p>
     * All methods return {@code this} to enable method chaining. Must call {@link #register()} to finalize
     * registrations.
     * <p>
     * <strong>Core API Methods:</strong>
     * <ul>
     * <li>{@link #reflectOn(Class[])} - Register classes for full reflection access</li>
     * <li>{@link #proxyFor(Class[])} - Register interfaces for JDK proxy creation</li>
     * <li>{@link #resources(String[])} - Register resource patterns for native image</li>
     * <li>{@link #handlers(String[])} - Register handler classes by name (dynamic)</li>
     * <li>{@link #register()} - Finalize and commit all registrations (required)</li>
     * </ul>
     */
    public static class HintBuilder {

        /**
         * Registers one or more classes for full reflection access.
         * <p>
         * Classes registered will have all fields, methods, and constructors accessible via reflection, including
         * private members.
         * <p>
         * <strong>Use Cases:</strong> Entity classes, DTO classes, service implementations, plugin classes
         *
         * @param classes The classes to register (varargs, null-safe)
         * @return This builder for method chaining
         */
        public HintBuilder reflectOn(Class<?>... classes) {
            if (classes != null && classes.length > 0) {
                synchronized (deferredReflectionClasses) {
                    Collections.addAll(deferredReflectionClasses, classes);
                }
            }
            return this;
        }

        /**
         * Registers JDK dynamic proxy interfaces for Native Image support.
         * <p>
         * Registers interface combinations for JDK dynamic proxy creation via {@link java.lang.reflect.Proxy}. GraalVM
         * requires ahead-of-time registration of all proxy interface combinations.
         * <p>
         * <strong>Use Cases:</strong> MyBatis mappers, AOP proxies, custom repository interfaces
         *
         * @param interfaces The interfaces to combine in a dynamic proxy (varargs, null-safe)
         * @return This builder for method chaining
         */
        public HintBuilder proxyFor(Class<?>... interfaces) {
            if (interfaces != null && interfaces.length > 0) {
                synchronized (deferredProxyInterfaces) {
                    deferredProxyInterfaces.add(interfaces);
                }
            }
            return this;
        }

        /**
         * Registers resource file patterns to be included in the Native Image binary.
         * <p>
         * By default, GraalVM Native Image does not include classpath resources. This method registers Ant-style
         * patterns (e.g., "**&#47;*.xml", "config/*.properties") to bundle resources.
         * <p>
         * <strong>Use Cases:</strong> Configuration files (YAML, properties, XML), templates, custom assets
         * <p>
         * <strong>Pattern Examples:</strong>
         * <ul>
         * <li>{@code **&#47;*.xml} - All XML files in any directory</li>
         * <li>{@code config/**&#47;*.properties} - Properties in config/ and subdirectories</li>
         * <li>{@code templates/**&#47;*.ftl} - Template files</li>
         * </ul>
         *
         * @param patterns The resource patterns to include (Ant-style, varargs, null-safe)
         * @return This builder for method chaining
         */
        public HintBuilder resources(String... patterns) {
            if (patterns != null && patterns.length > 0) {
                synchronized (deferredResourcePatterns) {
                    Collections.addAll(deferredResourcePatterns, patterns);
                }
            }
            return this;
        }

        /**
         * Registers handler classes by their fully qualified class names.
         * <p>
         * Useful for dynamically determined handler class names (e.g., loaded from configuration files). The class
         * loader will attempt to load each class during AOT processing.
         * <p>
         * <strong>Use Cases:</strong> Plugin systems, configurable handlers, dynamically loaded implementations
         * <p>
         * <strong>Error Handling:</strong> If a class cannot be resolved, a warning is logged but build continues.
         *
         * @param classNames Fully qualified class names of handlers (varargs, null-safe)
         * @return This builder for method chaining
         */
        public HintBuilder handlers(String... classNames) {
            if (classNames != null && classNames.length > 0) {
                synchronized (deferredHandlerClassNames) {
                    Collections.addAll(deferredHandlerClassNames, classNames);
                }
            }
            return this;
        }

        /**
         * Finalizes and commits all accumulated hint registrations to the deferred queues.
         * <p>
         * <strong>IMPORTANT:</strong> This method must be called to commit registrations. Without calling this, no
         * hints will be registered.
         * <p>
         * This method performs:
         * <ul>
         * <li>Validates that at least one registration has been made</li>
         * <li>Logs a summary of all registered hints</li>
         * <li>Marks the builder as completed</li>
         * </ul>
         * <p>
         * This is a terminal operation - the builder should not be reused after calling.
         * <p>
         * <strong>Usage:</strong>
         *
         * <pre>
         * NativeImageRuntimeHints.hints().reflectOn(User.class).proxyFor(UserMapper.class).register(); // Must call this!
         * </pre>
         *
         * @throws IllegalStateException if no hints have been registered
         */
        public void register() {
            // Validate that at least one registration was made
            int total = deferredReflectionClasses.size() + deferredProxyInterfaces.size()
                    + deferredResourcePatterns.size() + deferredHandlerClassNames.size();

            if (total == 0) {
                Logger.warn(
                        "Warning: register() called but no hints were registered. "
                                + "This may indicate a configuration error.");
                return;
            }

            // Log summary of registrations
            StringBuilder builder = new StringBuilder();
            builder.append("Hint registration completed - Summary: ");

            if (!deferredReflectionClasses.isEmpty()) {
                builder.append(deferredReflectionClasses.size()).append(" reflection classes, ");
            }
            if (!deferredProxyInterfaces.isEmpty()) {
                builder.append(deferredProxyInterfaces.size()).append(" proxy interface combinations, ");
            }
            if (!deferredResourcePatterns.isEmpty()) {
                builder.append(deferredResourcePatterns.size()).append(" resource patterns, ");
            }
            if (!deferredHandlerClassNames.isEmpty()) {
                builder.append(deferredHandlerClassNames.size()).append(" handler classes");
            }

            // Remove trailing comma and space if present
            String text = builder.toString();
            if (text.endsWith(Symbol.DOT + Symbol.SPACE)) {
                text = text.substring(0, text.length() - 2);
            }

            Logger.debug(text);
        }
    }

}
