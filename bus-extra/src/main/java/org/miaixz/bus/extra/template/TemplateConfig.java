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
package org.miaixz.bus.extra.template;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import org.miaixz.bus.core.lang.Charset;

/**
 * Configuration class for template engines. This class holds various settings required for initializing and using
 * template engines, such as character encoding, template paths, resource loading mode, and custom template providers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TemplateConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852288273095L;

    /**
     * Default template configuration instance. Uses UTF-8 encoding and {@link ResourceMode#STRING} as the default
     * resource loading mode.
     */
    public static final TemplateConfig DEFAULT = new TemplateConfig();

    /**
     * Character set for template files.
     */
    private java.nio.charset.Charset charset;
    /**
     * Path to the template files. This can be a relative path if {@link ResourceMode#CLASSPATH} or
     * {@link ResourceMode#WEB_ROOT} is used.
     */
    private String path;
    /**
     * Resource loading mode for templates. Defines how template resources are located and loaded.
     */
    private ResourceMode resourceMode;
    /**
     * Custom template provider class. Allows specifying a particular template engine implementation when multiple are
     * available.
     */
    private Class<? extends TemplateProvider> provider;

    /**
     * Indicates whether template caching is enabled.
     */
    private boolean useCache = true;

    /**
     * Default constructor. Initializes with UTF-8 encoding and {@link ResourceMode#STRING} as the default resource
     * loading mode.
     */
    public TemplateConfig() {
        this(null);
    }

    /**
     * Constructs a new TemplateConfig with the specified template path. Uses UTF-8 encoding and
     * {@link ResourceMode#STRING} as the default resource loading mode.
     *
     * @param path The path to the template files. Can be a relative path depending on the resource mode.
     */
    public TemplateConfig(final String path) {
        this(path, ResourceMode.STRING);
    }

    /**
     * Constructs a new TemplateConfig with the specified template path and resource loading mode. Uses UTF-8 encoding
     * by default.
     *
     * @param path         The path to the template files. Can be a relative path depending on the resource mode.
     * @param resourceMode The resource loading mode for templates.
     */
    public TemplateConfig(final String path, final ResourceMode resourceMode) {
        this(Charset.UTF_8, path, resourceMode);
    }

    /**
     * Constructs a new TemplateConfig with the specified character set, template path, and resource loading mode.
     *
     * @param charset      The character set for template files.
     * @param path         The path to the template files. Can be a relative path depending on the resource mode.
     * @param resourceMode The resource loading mode for templates.
     */
    public TemplateConfig(final java.nio.charset.Charset charset, final String path, final ResourceMode resourceMode) {
        this.charset = charset;
        this.path = path;
        this.resourceMode = resourceMode;
    }

    /**
     * Retrieves the character set used for template files.
     *
     * @return The character set.
     */
    public java.nio.charset.Charset getCharset() {
        return charset;
    }

    /**
     * Sets the character set for template files.
     *
     * @param charset The character set to set.
     */
    public void setCharset(final java.nio.charset.Charset charset) {
        this.charset = charset;
    }

    /**
     * Retrieves the character set name as a string.
     *
     * @return The name of the character set, or {@code null} if not set.
     */
    public String getCharsetString() {
        if (null == this.charset) {
            return null;
        }
        return this.charset.toString();
    }

    /**
     * Retrieves the path to the template files. This can be a relative path depending on the configured
     * {@link ResourceMode}.
     *
     * @return The template path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path to the template files. This can be a relative path if {@link ResourceMode#CLASSPATH} or
     * {@link ResourceMode#WEB_ROOT} is used.
     *
     * @param path The template path to set.
     */
    public void setPath(final String path) {
        this.path = path;
    }

    /**
     * Retrieves the resource loading mode for templates.
     *
     * @return The resource loading mode.
     */
    public ResourceMode getResourceMode() {
        return resourceMode;
    }

    /**
     * Sets the resource loading mode for templates.
     *
     * @param resourceMode The resource loading mode to set.
     */
    public void setResourceMode(final ResourceMode resourceMode) {
        this.resourceMode = resourceMode;
    }

    /**
     * Retrieves the custom template provider class. If {@code null}, the system will automatically determine the
     * appropriate engine.
     *
     * @return The custom template provider class, or {@code null} for automatic detection.
     */
    public Class<? extends TemplateProvider> getProvider() {
        return provider;
    }

    /**
     * Sets the custom template provider class. If {@code null}, the system will automatically determine the appropriate
     * engine.
     *
     * @param provider The custom template provider class to set, or {@code null} for automatic detection.
     * @return This TemplateConfig instance for method chaining.
     */
    public TemplateConfig setProvider(final Class<? extends TemplateProvider> provider) {
        this.provider = provider;
        return this;
    }

    /**
     * Checks if template caching is enabled.
     *
     * @return {@code true} if caching is enabled, {@code false} otherwise.
     */
    public boolean isUseCache() {
        return useCache;
    }

    /**
     * Sets whether template caching should be enabled.
     *
     * @param useCache {@code true} to enable caching, {@code false} to disable.
     * @return This TemplateConfig instance for method chaining.
     */
    public TemplateConfig setUseCache(boolean useCache) {
        this.useCache = useCache;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TemplateConfig that = (TemplateConfig) o;
        return Objects.equals(charset, that.charset) && Objects.equals(path, that.path)
                && resourceMode == that.resourceMode && Objects.equals(provider, that.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(charset, path, resourceMode, provider);
    }

    /**
     * Enumeration defining different modes for loading template resources.
     */
    public enum ResourceMode {
        /**
         * Loads templates from the ClassPath.
         */
        CLASSPATH,
        /**
         * Loads templates from a file system directory.
         */
        FILE,
        /**
         * Loads templates from the WebRoot directory (e.g., in a web application).
         */
        WEB_ROOT,
        /**
         * Treats the template content as a direct string.
         */
        STRING,
        /**
         * Attempts to load templates using a composite strategy, trying File, ClassPath, WebRoot, and String modes in
         * sequence.
         */
        COMPOSITE
    }

}
